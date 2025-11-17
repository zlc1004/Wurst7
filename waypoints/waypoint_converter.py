#!/usr/bin/env python3
"""
BlockLogger to Xaero's Waypoints Converter
Converts BlockLogger JSON files to Xaero's waypoint format, grouping connected portal blocks.
"""

import json
import argparse
import sys
from collections import defaultdict
from typing import List, Tuple, Set, Dict, Any

def load_json_log(file_path: str) -> List[Dict[str, Any]]:
    """Load blocks from BlockLogger JSON file."""
    try:
        with open(file_path, 'r') as f:
            data = json.load(f)

        if 'blocks' not in data:
            print(f"Error: No 'blocks' key found in {file_path}")
            sys.exit(1)

        blocks = data['blocks']
        print(f"Loaded {len(blocks)} blocks from {file_path}")
        return blocks

    except FileNotFoundError:
        print(f"Error: File {file_path} not found")
        sys.exit(1)
    except json.JSONDecodeError as e:
        print(f"Error: Invalid JSON in {file_path}: {e}")
        sys.exit(1)

def are_blocks_connected(block1: Tuple[int, int, int], block2: Tuple[int, int, int]) -> bool:
    """Check if two blocks are adjacent (connected)."""
    x1, y1, z1 = block1
    x2, y2, z2 = block2

    # Calculate Manhattan distance
    distance = abs(x1 - x2) + abs(y1 - y2) + abs(z1 - z2)

    # Blocks are connected if they're adjacent (distance = 1)
    return distance == 1

def is_within_portal_bounds(blocks: List[Tuple[int, int, int]]) -> bool:
    """Check if blocks form a valid portal (max 10x10)."""
    if not blocks:
        return False

    # Get bounding box
    min_x = min(block[0] for block in blocks)
    max_x = max(block[0] for block in blocks)
    min_y = min(block[1] for block in blocks)
    max_y = max(block[1] for block in blocks)
    min_z = min(block[2] for block in blocks)
    max_z = max(block[2] for block in blocks)

    # Check if any dimension exceeds 10 blocks
    width_x = max_x - min_x + 1
    width_y = max_y - min_y + 1
    width_z = max_z - min_z + 1

    return width_x <= 10 and width_y <= 10 and width_z <= 10

def group_connected_blocks(blocks: List[Dict[str, Any]]) -> List[List[Tuple[int, int, int]]]:
    """Group connected portal blocks into individual portals."""
    # Convert to coordinate tuples
    coords = [(block['x'], block['y'], block['z']) for block in blocks]

    # Group connected blocks using flood fill algorithm
    visited = set()
    groups = []

    for coord in coords:
        if coord in visited:
            continue

        # Start a new group with flood fill
        group = []
        stack = [coord]

        while stack:
            current = stack.pop()
            if current in visited:
                continue

            visited.add(current)
            group.append(current)

            # Find all unvisited connected blocks
            for other_coord in coords:
                if other_coord not in visited and are_blocks_connected(current, other_coord):
                    stack.append(other_coord)

        # Only add valid portal groups
        if is_within_portal_bounds(group):
            groups.append(group)
        else:
            print(f"Warning: Portal group too large ({len(group)} blocks), skipping")

    return groups

def calculate_portal_center(portal_blocks: List[Tuple[int, int, int]]) -> Tuple[int, int, int]:
    """Calculate the center coordinates of a portal."""
    if not portal_blocks:
        return (0, 0, 0)

    sum_x = sum(block[0] for block in portal_blocks)
    sum_y = sum(block[1] for block in portal_blocks)
    sum_z = sum(block[2] for block in portal_blocks)

    count = len(portal_blocks)

    # Return rounded center coordinates
    center_x = round(sum_x / count)
    center_y = round(sum_y / count)
    center_z = round(sum_z / count)

    return (center_x, center_y, center_z)

def format_coordinate(coord: int) -> str:
    """Format coordinate, replacing negative sign with 'n'."""
    if coord < 0:
        return f"n{abs(coord)}"
    else:
        return str(coord)

def create_waypoint_name(x: int, z: int) -> str:
    """Create waypoint name in format 'portal {x} {z}' with negative coordinates as 'n'."""
    x_str = format_coordinate(x)
    z_str = format_coordinate(z)
    return f"portal {x_str} {z_str}"

def generate_xaero_waypoint(name: str, x: int, y: int, z: int) -> str:
    """Generate a waypoint in Xaero's format."""
    # Xaero's waypoint format:
    # waypoint:name:initials:x:y:z:color:disabled:type:set:rotate_on_tp:tp_yaw:visibility_type:destination

    # Use single letter "P" for portal initials
    initials = "P"

    return f"waypoint:{name}:{initials}:{x}:{y}:{z}:13:false:0:gui.xaero_default:false:0:0:false"

def parse_file_for_coordinates(file_path: str) -> Set[Tuple[int, int, int]]:
    """Parse a file (JSON or waypoint format) and extract portal center coordinates."""
    portal_coords = set()

    try:
        # Determine file type by extension
        if file_path.lower().endswith('.json'):
            # Parse JSON file (BlockLogger format)
            blocks = load_json_log(file_path)

            if not blocks:
                print(f"No blocks found in JSON file {file_path}")
                return portal_coords

            # Group connected blocks into portals (same logic as normal conversion)
            portal_groups = group_connected_blocks(blocks)

            # Calculate center coordinates for each portal group
            for portal_blocks in portal_groups:
                center_x, center_y, center_z = calculate_portal_center(portal_blocks)
                portal_coords.add((center_x, center_y, center_z))

            print(f"Loaded {len(portal_coords)} portals from JSON file {file_path}")

        else:
            # Parse waypoint txt file
            with open(file_path, 'r') as f:
                for line in f:
                    line = line.strip()
                    # Skip comments and empty lines
                    if line.startswith('#') or not line:
                        continue

                    # Parse waypoint line: waypoint:name:initials:x:y:z:color:disabled:type:set:rotate_on_tp:tp_yaw:visibility_type:destination
                    parts = line.split(':')
                    if len(parts) >= 6 and parts[0] == 'waypoint':
                        try:
                            x = int(parts[3])
                            y = int(parts[4])
                            z = int(parts[5])
                            portal_coords.add((x, y, z))
                        except ValueError:
                            continue  # Skip invalid coordinate lines

            print(f"Loaded {len(portal_coords)} portal coordinates from waypoint file {file_path}")

        return portal_coords

    except FileNotFoundError:
        print(f"Error: File {file_path} not found")
        sys.exit(1)
    except IOError as e:
        print(f"Error reading {file_path}: {e}")
        sys.exit(1)

def find_new_portals(before_coords: Set[Tuple[int, int, int]], after_coords: Set[Tuple[int, int, int]]) -> Set[Tuple[int, int, int]]:
    """Find portal coordinates that are in 'after' but not in 'before'."""
    new_coords = after_coords - before_coords
    print(f"Found {len(new_coords)} new portals")
    return new_coords

def convert_to_waypoints_diff_mode(before_file: str, after_file: str, output_file: str):
    """Convert difference between two files (JSON or waypoint format) to new waypoints."""
    print(f"Comparing {before_file} and {after_file}, outputting diff to {output_file}")

    # Parse both files (can be JSON or waypoint format)
    before_coords = parse_file_for_coordinates(before_file)
    after_coords = parse_file_for_coordinates(after_file)

    # Find new portals
    new_coords = find_new_portals(before_coords, after_coords)

    if not new_coords:
        print("No new portals found")
        # Still create an empty waypoint file with header
        waypoints = []
        waypoints.append("#")
        waypoints.append("#waypoint:name:initials:x:y:z:color:disabled:type:set:rotate_on_tp:tp_yaw:visibility_type:destination")
        waypoints.append("#")
    else:
        # Generate waypoints for new portals
        waypoints = []
        waypoints.append("#")
        waypoints.append("#waypoint:name:initials:x:y:z:color:disabled:type:set:rotate_on_tp:tp_yaw:visibility_type:destination")
        waypoints.append("#")

        for i, (x, y, z) in enumerate(sorted(new_coords), 1):
            # Create waypoint name
            waypoint_name = create_waypoint_name(x, z)

            # Generate Xaero waypoint
            waypoint = generate_xaero_waypoint(waypoint_name, x, y, z)
            waypoints.append(waypoint)

            print(f"New Portal {i}: ({x}, {y}, {z})")

    # Write to output file
    try:
        with open(output_file, 'w') as f:
            f.write('\n'.join(waypoints) + '\n')

        print(f"Successfully created {len(new_coords)} new waypoints in {output_file}")

    except IOError as e:
        print(f"Error writing to {output_file}: {e}")
        sys.exit(1)

def convert_to_waypoints(input_file: str, output_file: str):
    """Convert BlockLogger JSON to Xaero's waypoints."""
    print(f"Converting {input_file} to {output_file}")

    # Load blocks from JSON
    blocks = load_json_log(input_file)

    if not blocks:
        print("No blocks found in input file")
        return

    # Group connected blocks into portals
    print("Grouping connected portal blocks...")
    portal_groups = group_connected_blocks(blocks)

    print(f"Found {len(portal_groups)} separate portals")

    # Generate waypoints
    waypoints = []
    waypoints.append("#")
    waypoints.append("#waypoint:name:initials:x:y:z:color:disabled:type:set:rotate_on_tp:tp_yaw:visibility_type:destination")
    waypoints.append("#")

    for i, portal_blocks in enumerate(portal_groups, 1):
        # Calculate center of portal
        center_x, center_y, center_z = calculate_portal_center(portal_blocks)

        # Create waypoint name
        waypoint_name = create_waypoint_name(center_x, center_z)

        # Generate Xaero waypoint
        waypoint = generate_xaero_waypoint(waypoint_name, center_x, center_y, center_z)
        waypoints.append(waypoint)

        print(f"Portal {i}: {len(portal_blocks)} blocks at center ({center_x}, {center_y}, {center_z})")

    # Write to output file
    try:
        with open(output_file, 'w') as f:
            f.write('\n'.join(waypoints) + '\n')

        print(f"Successfully created {len(portal_groups)} waypoints in {output_file}")

    except IOError as e:
        print(f"Error writing to {output_file}: {e}")
        sys.exit(1)

def main():
    """Main CLI function."""
    parser = argparse.ArgumentParser(
        description="Convert BlockLogger JSON files to Xaero's waypoint format or compare files for new portals",
        formatter_class=argparse.RawDescriptionHelpFormatter,
        epilog="""
Examples:
  Normal mode:
    python waypoint_converter.py -i portal_log.json -o waypoints.txt
    python waypoint_converter.py --input ./logs/1763173039_nether_portal.json --output ./waypoints/portals.txt

  Diff mode (compare JSON files):
    python waypoint_converter.py -d --before old_log.json --after new_log.json -o new_portals.txt

  Diff mode (compare waypoint files):
    python waypoint_converter.py -d --before old_waypoints.txt --after new_waypoints.txt -o new_portals.txt
        """
    )

    parser.add_argument(
        '-i', '--input',
        help='Input BlockLogger JSON file path (required for normal mode)'
    )

    parser.add_argument(
        '-o', '--output',
        required=True,
        help='Output waypoints txt file path'
    )

    parser.add_argument(
        '-d', '--diff',
        action='store_true',
        help='Enable diff mode to compare two waypoint files'
    )

    parser.add_argument(
        '--before',
        help='Before file path - JSON or waypoint format (required for diff mode)'
    )

    parser.add_argument(
        '--after',
        help='After file path - JSON or waypoint format (required for diff mode)'
    )

    args = parser.parse_args()

    # Validate arguments based on mode
    if args.diff:
        # Diff mode: require --before and --after
        if not args.before or not args.after:
            parser.error("Diff mode requires both --before and --after arguments")

        # Convert files in diff mode
        convert_to_waypoints_diff_mode(args.before, args.after, args.output)
    else:
        # Normal mode: require --input
        if not args.input:
            parser.error("Normal mode requires --input argument")

        # Convert files in normal mode
        convert_to_waypoints(args.input, args.output)

if __name__ == '__main__':
    main()
