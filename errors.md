---- Minecraft Network Protocol Error Report ----
// Wait, was the last bit one or zero?

Time: 2025-11-13 17:22:01
Description: Firing Wurst event

java.lang.NullPointerException: Cannot invoke "net.minecraft.class_634.method_2871(java.util.UUID)" because the return value of "net.minecraft.class_310.method_1562()" is null
	at knot//net.wurstclient.hacks.GamemodeNotifierHack.onReceivedPacket(GamemodeNotifierHack.java:71)
	at knot//net.wurstclient.events.PacketInputListener$PacketInputEvent.fire(PacketInputListener.java:40)
	at knot//net.wurstclient.event.EventManager.fireImpl(EventManager.java:69)
	at knot//net.wurstclient.event.EventManager.fire(EventManager.java:42)
	at knot//net.minecraft.class_2535.handler$zbb000$appleskin$onChannelRead0(class_2535.java:781)
	at knot//net.minecraft.class_2535.method_10770(class_2535.java:193)
	at knot//net.minecraft.class_2535.channelRead0(class_2535.java:69)
	at knot//io.netty.channel.SimpleChannelInboundHandler.channelRead(SimpleChannelInboundHandler.java:99)
	at knot//io.netty.channel.AbstractChannelHandlerContext.invokeChannelRead(AbstractChannelHandlerContext.java:444)
	at knot//io.netty.channel.AbstractChannelHandlerContext.invokeChannelRead(AbstractChannelHandlerContext.java:420)
	at knot//io.netty.channel.AbstractChannelHandlerContext.fireChannelRead(AbstractChannelHandlerContext.java:412)
	at knot//io.netty.handler.codec.MessageToMessageDecoder.channelRead(MessageToMessageDecoder.java:103)
	at knot//io.netty.channel.AbstractChannelHandlerContext.invokeChannelRead(AbstractChannelHandlerContext.java:444)
	at knot//io.netty.channel.AbstractChannelHandlerContext.invokeChannelRead(AbstractChannelHandlerContext.java:420)
	at knot//io.netty.channel.AbstractChannelHandlerContext.fireChannelRead(AbstractChannelHandlerContext.java:412)
	at knot//io.netty.handler.codec.ByteToMessageDecoder.fireChannelRead(ByteToMessageDecoder.java:346)
	at knot//io.netty.handler.codec.ByteToMessageDecoder.channelRead(ByteToMessageDecoder.java:318)
	at knot//io.netty.channel.AbstractChannelHandlerContext.invokeChannelRead(AbstractChannelHandlerContext.java:444)
	at knot//io.netty.channel.AbstractChannelHandlerContext.invokeChannelRead(AbstractChannelHandlerContext.java:420)
	at knot//io.netty.channel.AbstractChannelHandlerContext.fireChannelRead(AbstractChannelHandlerContext.java:412)
	at knot//io.netty.handler.flow.FlowControlHandler.dequeue(FlowControlHandler.java:202)
	at knot//io.netty.handler.flow.FlowControlHandler.channelRead(FlowControlHandler.java:164)
	at knot//io.netty.channel.AbstractChannelHandlerContext.invokeChannelRead(AbstractChannelHandlerContext.java:442)
	at knot//io.netty.channel.AbstractChannelHandlerContext.invokeChannelRead(AbstractChannelHandlerContext.java:420)
	at knot//io.netty.channel.AbstractChannelHandlerContext.fireChannelRead(AbstractChannelHandlerContext.java:412)
	at knot//io.netty.handler.codec.ByteToMessageDecoder.fireChannelRead(ByteToMessageDecoder.java:346)
	at knot//io.netty.handler.codec.ByteToMessageDecoder.channelRead(ByteToMessageDecoder.java:318)
	at knot//io.netty.channel.AbstractChannelHandlerContext.invokeChannelRead(AbstractChannelHandlerContext.java:444)
	at knot//io.netty.channel.AbstractChannelHandlerContext.invokeChannelRead(AbstractChannelHandlerContext.java:420)
	at knot//io.netty.channel.AbstractChannelHandlerContext.fireChannelRead(AbstractChannelHandlerContext.java:412)
	at knot//io.netty.handler.codec.ByteToMessageDecoder.fireChannelRead(ByteToMessageDecoder.java:346)
	at knot//io.netty.handler.codec.ByteToMessageDecoder.fireChannelRead(ByteToMessageDecoder.java:333)
	at knot//io.netty.handler.codec.ByteToMessageDecoder.callDecode(ByteToMessageDecoder.java:454)
	at knot//io.netty.handler.codec.ByteToMessageDecoder.channelRead(ByteToMessageDecoder.java:290)
	at knot//io.netty.channel.AbstractChannelHandlerContext.invokeChannelRead(AbstractChannelHandlerContext.java:444)
	at knot//io.netty.channel.AbstractChannelHandlerContext.invokeChannelRead(AbstractChannelHandlerContext.java:420)
	at knot//io.netty.channel.AbstractChannelHandlerContext.fireChannelRead(AbstractChannelHandlerContext.java:412)
	at knot//io.netty.handler.codec.MessageToMessageDecoder.channelRead(MessageToMessageDecoder.java:103)
	at knot//io.netty.channel.AbstractChannelHandlerContext.invokeChannelRead(AbstractChannelHandlerContext.java:444)
	at knot//io.netty.channel.AbstractChannelHandlerContext.invokeChannelRead(AbstractChannelHandlerContext.java:420)
	at knot//io.netty.channel.AbstractChannelHandlerContext.fireChannelRead(AbstractChannelHandlerContext.java:412)
	at knot//io.netty.handler.timeout.IdleStateHandler.channelRead(IdleStateHandler.java:286)
	at knot//io.netty.channel.AbstractChannelHandlerContext.invokeChannelRead(AbstractChannelHandlerContext.java:442)
	at knot//io.netty.channel.AbstractChannelHandlerContext.invokeChannelRead(AbstractChannelHandlerContext.java:420)
	at knot//io.netty.channel.AbstractChannelHandlerContext.fireChannelRead(AbstractChannelHandlerContext.java:412)
	at knot//io.netty.channel.DefaultChannelPipeline$HeadContext.channelRead(DefaultChannelPipeline.java:1410)
	at knot//io.netty.channel.AbstractChannelHandlerContext.invokeChannelRead(AbstractChannelHandlerContext.java:440)
	at knot//io.netty.channel.AbstractChannelHandlerContext.invokeChannelRead(AbstractChannelHandlerContext.java:420)
	at knot//io.netty.channel.DefaultChannelPipeline.fireChannelRead(DefaultChannelPipeline.java:919)
	at knot//io.netty.channel.nio.AbstractNioByteChannel$NioByteUnsafe.read(AbstractNioByteChannel.java:166)
	at knot//io.netty.channel.nio.NioEventLoop.processSelectedKey(NioEventLoop.java:788)
	at knot//io.netty.channel.nio.NioEventLoop.processSelectedKeysOptimized(NioEventLoop.java:724)
	at knot//io.netty.channel.nio.NioEventLoop.processSelectedKeys(NioEventLoop.java:650)
	at knot//io.netty.channel.nio.NioEventLoop.run(NioEventLoop.java:562)
	at knot//io.netty.util.concurrent.SingleThreadEventExecutor$4.run(SingleThreadEventExecutor.java:997)
	at knot//io.netty.util.internal.ThreadExecutorMap$2.run(ThreadExecutorMap.java:74)
	at java.base/java.lang.Thread.run(Thread.java:1575)


A detailed walkthrough of the error, its code path and all known details is as follows:
---------------------------------------------------------------------------------------

-- Head --
Thread: Netty Client IO #5
Stacktrace:
	at knot//net.wurstclient.hacks.GamemodeNotifierHack.onReceivedPacket(GamemodeNotifierHack.java:71)
	at knot//net.wurstclient.events.PacketInputListener$PacketInputEvent.fire(PacketInputListener.java:40)

-- Affected event --
Details:
	Event class: net.wurstclient.events.PacketInputListener$PacketInputEvent
Stacktrace:
	at knot//net.wurstclient.event.EventManager.fireImpl(EventManager.java:69)
	at knot//net.wurstclient.event.EventManager.fire(EventManager.java:42)
	at knot//net.minecraft.class_2535.handler$zbb000$appleskin$onChannelRead0(class_2535.java:781)
	at knot//net.minecraft.class_2535.method_10770(class_2535.java:193)
	at knot//net.minecraft.class_2535.channelRead0(class_2535.java:69)
	at knot//io.netty.channel.SimpleChannelInboundHandler.channelRead(SimpleChannelInboundHandler.java:99)
	at knot//io.netty.channel.AbstractChannelHandlerContext.invokeChannelRead(AbstractChannelHandlerContext.java:444)
	at knot//io.netty.channel.AbstractChannelHandlerContext.invokeChannelRead(AbstractChannelHandlerContext.java:420)
	at knot//io.netty.channel.AbstractChannelHandlerContext.fireChannelRead(AbstractChannelHandlerContext.java:412)
	at knot//io.netty.handler.codec.MessageToMessageDecoder.channelRead(MessageToMessageDecoder.java:103)
	at knot//io.netty.channel.AbstractChannelHandlerContext.invokeChannelRead(AbstractChannelHandlerContext.java:444)
	at knot//io.netty.channel.AbstractChannelHandlerContext.invokeChannelRead(AbstractChannelHandlerContext.java:420)
	at knot//io.netty.channel.AbstractChannelHandlerContext.fireChannelRead(AbstractChannelHandlerContext.java:412)
	at knot//io.netty.handler.codec.ByteToMessageDecoder.fireChannelRead(ByteToMessageDecoder.java:346)
	at knot//io.netty.handler.codec.ByteToMessageDecoder.channelRead(ByteToMessageDecoder.java:318)
	at knot//io.netty.channel.AbstractChannelHandlerContext.invokeChannelRead(AbstractChannelHandlerContext.java:444)
	at knot//io.netty.channel.AbstractChannelHandlerContext.invokeChannelRead(AbstractChannelHandlerContext.java:420)
	at knot//io.netty.channel.AbstractChannelHandlerContext.fireChannelRead(AbstractChannelHandlerContext.java:412)
	at knot//io.netty.handler.flow.FlowControlHandler.dequeue(FlowControlHandler.java:202)
	at knot//io.netty.handler.flow.FlowControlHandler.channelRead(FlowControlHandler.java:164)
	at knot//io.netty.channel.AbstractChannelHandlerContext.invokeChannelRead(AbstractChannelHandlerContext.java:442)
	at knot//io.netty.channel.AbstractChannelHandlerContext.invokeChannelRead(AbstractChannelHandlerContext.java:420)
	at knot//io.netty.channel.AbstractChannelHandlerContext.fireChannelRead(AbstractChannelHandlerContext.java:412)
	at knot//io.netty.handler.codec.ByteToMessageDecoder.fireChannelRead(ByteToMessageDecoder.java:346)
	at knot//io.netty.handler.codec.ByteToMessageDecoder.channelRead(ByteToMessageDecoder.java:318)
	at knot//io.netty.channel.AbstractChannelHandlerContext.invokeChannelRead(AbstractChannelHandlerContext.java:444)
	at knot//io.netty.channel.AbstractChannelHandlerContext.invokeChannelRead(AbstractChannelHandlerContext.java:420)
	at knot//io.netty.channel.AbstractChannelHandlerContext.fireChannelRead(AbstractChannelHandlerContext.java:412)
	at knot//io.netty.handler.codec.ByteToMessageDecoder.fireChannelRead(ByteToMessageDecoder.java:346)
	at knot//io.netty.handler.codec.ByteToMessageDecoder.fireChannelRead(ByteToMessageDecoder.java:333)
	at knot//io.netty.handler.codec.ByteToMessageDecoder.callDecode(ByteToMessageDecoder.java:454)
	at knot//io.netty.handler.codec.ByteToMessageDecoder.channelRead(ByteToMessageDecoder.java:290)
	at knot//io.netty.channel.AbstractChannelHandlerContext.invokeChannelRead(AbstractChannelHandlerContext.java:444)
	at knot//io.netty.channel.AbstractChannelHandlerContext.invokeChannelRead(AbstractChannelHandlerContext.java:420)
	at knot//io.netty.channel.AbstractChannelHandlerContext.fireChannelRead(AbstractChannelHandlerContext.java:412)
	at knot//io.netty.handler.codec.MessageToMessageDecoder.channelRead(MessageToMessageDecoder.java:103)
	at knot//io.netty.channel.AbstractChannelHandlerContext.invokeChannelRead(AbstractChannelHandlerContext.java:444)
	at knot//io.netty.channel.AbstractChannelHandlerContext.invokeChannelRead(AbstractChannelHandlerContext.java:420)
	at knot//io.netty.channel.AbstractChannelHandlerContext.fireChannelRead(AbstractChannelHandlerContext.java:412)
	at knot//io.netty.handler.timeout.IdleStateHandler.channelRead(IdleStateHandler.java:286)
	at knot//io.netty.channel.AbstractChannelHandlerContext.invokeChannelRead(AbstractChannelHandlerContext.java:442)
	at knot//io.netty.channel.AbstractChannelHandlerContext.invokeChannelRead(AbstractChannelHandlerContext.java:420)
	at knot//io.netty.channel.AbstractChannelHandlerContext.fireChannelRead(AbstractChannelHandlerContext.java:412)
	at knot//io.netty.channel.DefaultChannelPipeline$HeadContext.channelRead(DefaultChannelPipeline.java:1410)
	at knot//io.netty.channel.AbstractChannelHandlerContext.invokeChannelRead(AbstractChannelHandlerContext.java:440)
	at knot//io.netty.channel.AbstractChannelHandlerContext.invokeChannelRead(AbstractChannelHandlerContext.java:420)
	at knot//io.netty.channel.DefaultChannelPipeline.fireChannelRead(DefaultChannelPipeline.java:919)
	at knot//io.netty.channel.nio.AbstractNioByteChannel$NioByteUnsafe.read(AbstractNioByteChannel.java:166)
	at knot//io.netty.channel.nio.NioEventLoop.processSelectedKey(NioEventLoop.java:788)
	at knot//io.netty.channel.nio.NioEventLoop.processSelectedKeysOptimized(NioEventLoop.java:724)
	at knot//io.netty.channel.nio.NioEventLoop.processSelectedKeys(NioEventLoop.java:650)
	at knot//io.netty.channel.nio.NioEventLoop.run(NioEventLoop.java:562)
	at knot//io.netty.util.concurrent.SingleThreadEventExecutor$4.run(SingleThreadEventExecutor.java:997)
	at knot//io.netty.util.internal.ThreadExecutorMap$2.run(ThreadExecutorMap.java:74)
	at java.base/java.lang.Thread.run(Thread.java:1575)

-- Connection --
Details:
	Protocol: play
	Flow: CLIENTBOUND
	Server type: OTHER
	Server brand: fabric
Stacktrace:
	at knot//net.minecraft.class_2547.method_55622(class_2547.java:33)
	at knot//net.minecraft.class_2600.method_59803(class_2600.java:62)
	at knot//net.minecraft.class_8673.method_60882(class_8673.java:136)
	at knot//net.minecraft.class_8673.method_60881(class_8673.java:127)
	at knot//net.minecraft.class_2535.exceptionCaught(class_2535.java:161)
	at knot//io.netty.channel.AbstractChannelHandlerContext.invokeExceptionCaught(AbstractChannelHandlerContext.java:346)
	at knot//io.netty.channel.AbstractChannelHandlerContext.invokeChannelRead(AbstractChannelHandlerContext.java:447)
	at knot//io.netty.channel.AbstractChannelHandlerContext.invokeChannelRead(AbstractChannelHandlerContext.java:420)
	at knot//io.netty.channel.AbstractChannelHandlerContext.fireChannelRead(AbstractChannelHandlerContext.java:412)
	at knot//io.netty.handler.codec.MessageToMessageDecoder.channelRead(MessageToMessageDecoder.java:103)
	at knot//io.netty.channel.AbstractChannelHandlerContext.invokeChannelRead(AbstractChannelHandlerContext.java:444)
	at knot//io.netty.channel.AbstractChannelHandlerContext.invokeChannelRead(AbstractChannelHandlerContext.java:420)
	at knot//io.netty.channel.AbstractChannelHandlerContext.fireChannelRead(AbstractChannelHandlerContext.java:412)
	at knot//io.netty.handler.codec.ByteToMessageDecoder.fireChannelRead(ByteToMessageDecoder.java:346)
	at knot//io.netty.handler.codec.ByteToMessageDecoder.channelRead(ByteToMessageDecoder.java:318)
	at knot//io.netty.channel.AbstractChannelHandlerContext.invokeChannelRead(AbstractChannelHandlerContext.java:444)
	at knot//io.netty.channel.AbstractChannelHandlerContext.invokeChannelRead(AbstractChannelHandlerContext.java:420)
	at knot//io.netty.channel.AbstractChannelHandlerContext.fireChannelRead(AbstractChannelHandlerContext.java:412)
	at knot//io.netty.handler.flow.FlowControlHandler.dequeue(FlowControlHandler.java:202)
	at knot//io.netty.handler.flow.FlowControlHandler.channelRead(FlowControlHandler.java:164)
	at knot//io.netty.channel.AbstractChannelHandlerContext.invokeChannelRead(AbstractChannelHandlerContext.java:442)
	at knot//io.netty.channel.AbstractChannelHandlerContext.invokeChannelRead(AbstractChannelHandlerContext.java:420)
	at knot//io.netty.channel.AbstractChannelHandlerContext.fireChannelRead(AbstractChannelHandlerContext.java:412)
	at knot//io.netty.handler.codec.ByteToMessageDecoder.fireChannelRead(ByteToMessageDecoder.java:346)
	at knot//io.netty.handler.codec.ByteToMessageDecoder.channelRead(ByteToMessageDecoder.java:318)
	at knot//io.netty.channel.AbstractChannelHandlerContext.invokeChannelRead(AbstractChannelHandlerContext.java:444)
	at knot//io.netty.channel.AbstractChannelHandlerContext.invokeChannelRead(AbstractChannelHandlerContext.java:420)
	at knot//io.netty.channel.AbstractChannelHandlerContext.fireChannelRead(AbstractChannelHandlerContext.java:412)
	at knot//io.netty.handler.codec.ByteToMessageDecoder.fireChannelRead(ByteToMessageDecoder.java:346)
	at knot//io.netty.handler.codec.ByteToMessageDecoder.fireChannelRead(ByteToMessageDecoder.java:333)
	at knot//io.netty.handler.codec.ByteToMessageDecoder.callDecode(ByteToMessageDecoder.java:454)
	at knot//io.netty.handler.codec.ByteToMessageDecoder.channelRead(ByteToMessageDecoder.java:290)
	at knot//io.netty.channel.AbstractChannelHandlerContext.invokeChannelRead(AbstractChannelHandlerContext.java:444)
	at knot//io.netty.channel.AbstractChannelHandlerContext.invokeChannelRead(AbstractChannelHandlerContext.java:420)
	at knot//io.netty.channel.AbstractChannelHandlerContext.fireChannelRead(AbstractChannelHandlerContext.java:412)
	at knot//io.netty.handler.codec.MessageToMessageDecoder.channelRead(MessageToMessageDecoder.java:103)
	at knot//io.netty.channel.AbstractChannelHandlerContext.invokeChannelRead(AbstractChannelHandlerContext.java:444)
	at knot//io.netty.channel.AbstractChannelHandlerContext.invokeChannelRead(AbstractChannelHandlerContext.java:420)
	at knot//io.netty.channel.AbstractChannelHandlerContext.fireChannelRead(AbstractChannelHandlerContext.java:412)
	at knot//io.netty.handler.timeout.IdleStateHandler.channelRead(IdleStateHandler.java:286)
	at knot//io.netty.channel.AbstractChannelHandlerContext.invokeChannelRead(AbstractChannelHandlerContext.java:442)
	at knot//io.netty.channel.AbstractChannelHandlerContext.invokeChannelRead(AbstractChannelHandlerContext.java:420)
	at knot//io.netty.channel.AbstractChannelHandlerContext.fireChannelRead(AbstractChannelHandlerContext.java:412)
	at knot//io.netty.channel.DefaultChannelPipeline$HeadContext.channelRead(DefaultChannelPipeline.java:1410)
	at knot//io.netty.channel.AbstractChannelHandlerContext.invokeChannelRead(AbstractChannelHandlerContext.java:440)
	at knot//io.netty.channel.AbstractChannelHandlerContext.invokeChannelRead(AbstractChannelHandlerContext.java:420)
	at knot//io.netty.channel.DefaultChannelPipeline.fireChannelRead(DefaultChannelPipeline.java:919)
	at knot//io.netty.channel.nio.AbstractNioByteChannel$NioByteUnsafe.read(AbstractNioByteChannel.java:166)
	at knot//io.netty.channel.nio.NioEventLoop.processSelectedKey(NioEventLoop.java:788)
	at knot//io.netty.channel.nio.NioEventLoop.processSelectedKeysOptimized(NioEventLoop.java:724)
	at knot//io.netty.channel.nio.NioEventLoop.processSelectedKeys(NioEventLoop.java:650)
	at knot//io.netty.channel.nio.NioEventLoop.run(NioEventLoop.java:562)
	at knot//io.netty.util.concurrent.SingleThreadEventExecutor$4.run(SingleThreadEventExecutor.java:997)
	at knot//io.netty.util.internal.ThreadExecutorMap$2.run(ThreadExecutorMap.java:74)
	at java.base/java.lang.Thread.run(Thread.java:1575)

-- System Details --
Details:
	Minecraft Version: 1.21.1
	Minecraft Version ID: 1.21.1
	Operating System: Mac OS X (aarch64) version 26.0
	Java Version: 23.0.1, Homebrew
	Java VM Version: OpenJDK 64-Bit Server VM (mixed mode, sharing), Homebrew
	Memory: 681073360 bytes (649 MiB) / 1476395008 bytes (1408 MiB) up to 4294967296 bytes (4096 MiB)
	CPUs: 8
	Processor Vendor: Apple Inc.
	Processor Name: Apple M1
	Identifier: Apple Inc. Family 0x1b588bb3 Model 0 Stepping 0
	Microarchitecture: ARM64 SoC: Firestorm + Icestorm
	Frequency (GHz): 3.20
	Number of physical packages: 1
	Number of physical CPUs: 8
	Number of logical CPUs: 8
	Graphics card #0 name: Apple M1
	Graphics card #0 vendor: Apple (0x106b)
	Graphics card #0 VRAM (MiB): 0.00
	Graphics card #0 deviceId: unknown
	Graphics card #0 versionInfo: unknown
	Memory slot #0 capacity (MiB): 0.00
	Memory slot #0 clockSpeed (GHz): 0.00
	Memory slot #0 type: unknown
	Virtual memory max (MiB): 23552.00
	Virtual memory used (MiB): 19193.14
	Swap memory total (MiB): 7168.00
	Swap memory used (MiB): 5529.00
	Space in storage for jna.tmpdir (MiB): available: 35624.96, total: 948584.19
	Space in storage for org.lwjgl.system.SharedLibraryExtractPath (MiB): available: 35624.96, total: 948584.19
	Space in storage for io.netty.native.workdir (MiB): available: 35624.96, total: 948584.19
	Space in storage for java.io.tmpdir (MiB): available: 35624.96, total: 948584.19
	Space in storage for workdir (MiB): available: 35624.96, total: 948584.19
	JVM Flags: 18 total; -Xmx4096m -XX:+UnlockExperimentalVMOptions -XX:+UnlockDiagnosticVMOptions -XX:+UseG1GC -XX:G1MixedGCCountTarget=5 -XX:G1NewSizePercent=20 -XX:G1ReservePercent=20 -XX:MaxGCPauseMillis=50 -XX:G1HeapRegionSize=32m -XX:-OmitStackTraceInFastThrow -XX:-DontCompileHugeMethods -XX:MaxNodeLimit=240000 -XX:NodeLimitFudgeFactor=8000 -XX:TieredCompileTaskTimeout=10000 -XX:ReservedCodeCacheSize=400M -XX:NonNMethodCodeHeapSize=12M -XX:ProfiledCodeHeapSize=194M -XX:NmethodSweepActivity=1
	Fabric Mods: 
		appleskin: AppleSkin 7.51-MC1.21.1
		bookshelf: Bookshelf 21.1.78
		chat_heads: Chat Heads 0.14.2
		cloth-config: Cloth Config v15 15.0.140
			cloth-basic-math: cloth-basic-math 0.6.1
		continuity: Continuity 3.0.0+1.21
		crafttweaker: CraftTweaker 21.0.34
			org_javassist_javassist: javassist 3.29.0-GA
			org_reflections_reflections: reflections 0.10.2
		craterlib: CraterLib 2.1.5
		dynamic_fps: Dynamic FPS 3.9.5
			net_lostluma_battery: battery 1.3.0
		enchdesc: EnchantmentDescriptions 21.1.9
		entityculling: EntityCulling 1.9.3
			transition: TRansition 1.0.8
			trender: TRender 1.0.8
		fabric-api: Fabric API 0.116.7+1.21.1
			fabric-api-base: Fabric API Base 0.4.42+6573ed8c19
			fabric-api-lookup-api-v1: Fabric API Lookup API (v1) 1.6.71+b559734419
			fabric-biome-api-v1: Fabric Biome API (v1) 13.0.31+d527f9fd19
			fabric-block-api-v1: Fabric Block API (v1) 1.1.0+0bc3503219
			fabric-block-view-api-v2: Fabric BlockView API (v2) 1.0.11+ebb2264e19
			fabric-blockrenderlayer-v1: Fabric BlockRenderLayer Registration (v1) 1.1.52+0af3f5a719
			fabric-client-tags-api-v1: Fabric Client Tags 1.1.15+6573ed8c19
			fabric-command-api-v1: Fabric Command API (v1) 1.2.49+f71b366f19
			fabric-command-api-v2: Fabric Command API (v2) 2.2.28+6ced4dd919
			fabric-commands-v0: Fabric Commands (v0) 0.2.66+df3654b319
			fabric-content-registries-v0: Fabric Content Registries (v0) 8.0.19+b559734419
			fabric-convention-tags-v1: Fabric Convention Tags 2.1.5+7f945d5b19
			fabric-convention-tags-v2: Fabric Convention Tags (v2) 2.11.1+a406e79519
			fabric-crash-report-info-v1: Fabric Crash Report Info (v1) 0.2.29+0af3f5a719
			fabric-data-attachment-api-v1: Fabric Data Attachment API (v1) 1.4.5+6116a37819
			fabric-data-generation-api-v1: Fabric Data Generation API (v1) 20.2.34+37516cd619
			fabric-dimensions-v1: Fabric Dimensions API (v1) 4.0.1+65213ef819
			fabric-entity-events-v1: Fabric Entity Events (v1) 1.8.0+2b27e0a419
			fabric-events-interaction-v0: Fabric Events Interaction (v0) 0.7.13+ba9dae0619
			fabric-game-rule-api-v1: Fabric Game Rule API (v1) 1.0.53+6ced4dd919
			fabric-item-api-v1: Fabric Item API (v1) 11.2.0+3b3cb2e819
			fabric-item-group-api-v1: Fabric Item Group API (v1) 4.1.7+def88e3a19
			fabric-key-binding-api-v1: Fabric Key Binding API (v1) 1.0.47+0af3f5a719
			fabric-keybindings-v0: Fabric Key Bindings (v0) 0.2.45+df3654b319
			fabric-lifecycle-events-v1: Fabric Lifecycle Events (v1) 2.6.0+0865547519
			fabric-loot-api-v2: Fabric Loot API (v2) 3.0.15+3f89f5a519
			fabric-loot-api-v3: Fabric Loot API (v3) 1.0.3+3f89f5a519
			fabric-message-api-v1: Fabric Message API (v1) 6.0.14+8aaf3aca19
			fabric-model-loading-api-v1: Fabric Model Loading API (v1) 2.1.0+b4d813fc19
			fabric-networking-api-v1: Fabric Networking API (v1) 4.3.0+c7469b2119
			fabric-object-builder-api-v1: Fabric Object Builder API (v1) 15.2.1+40875a9319
			fabric-particles-v1: Fabric Particles (v1) 4.0.2+6573ed8c19
			fabric-recipe-api-v1: Fabric Recipe API (v1) 5.0.15+2475392c19
			fabric-registry-sync-v0: Fabric Registry Sync (v0) 5.3.1+e3eddc2119
			fabric-renderer-api-v1: Fabric Renderer API (v1) 3.4.1+b4d813fc19
			fabric-renderer-indigo: Fabric Renderer - Indigo 1.7.1+c705a49c19
			fabric-renderer-registries-v1: Fabric Renderer Registries (v1) 3.2.69+df3654b319
			fabric-rendering-data-attachment-v1: Fabric Rendering Data Attachment (v1) 0.3.49+73761d2e19
			fabric-rendering-fluids-v1: Fabric Rendering Fluids (v1) 3.1.6+1daea21519
			fabric-rendering-v0: Fabric Rendering (v0) 1.1.72+df3654b319
			fabric-rendering-v1: Fabric Rendering (v1) 5.1.0+ab4c25a019
			fabric-resource-conditions-api-v1: Fabric Resource Conditions API (v1) 4.3.0+8dc279b119
			fabric-resource-loader-v0: Fabric Resource Loader (v0) 1.3.1+5b5275af19
			fabric-screen-api-v1: Fabric Screen API (v1) 2.0.25+8b68f1c719
			fabric-screen-handler-api-v1: Fabric Screen Handler API (v1) 1.3.90+b559734419
			fabric-sound-api-v1: Fabric Sound API (v1) 1.0.23+6573ed8c19
			fabric-transfer-api-v1: Fabric Transfer API (v1) 5.4.3+c24bd99419
			fabric-transitive-access-wideners-v1: Fabric Transitive Access Wideners (v1) 6.2.0+45b9699719
		fabricloader: Fabric Loader 0.18.0
			mixinextras: MixinExtras 0.5.0
		ferritecore: FerriteCore 7.0.2-hotfix
		forgeconfigapiport: Forge Config API Port 21.1.6
			com_electronwill_night-config_core: core 3.8.0
			com_electronwill_night-config_toml: toml 3.8.0
		geckolib: GeckoLib 4 4.7.6
		iris: Iris 1.8.8+mc1.21.1
			io_github_douira_glsl-transformer: glsl-transformer 2.0.1
			org_anarres_jcpp: jcpp 1.4.14
			org_antlr_antlr4-runtime: antlr4-runtime 4.13.1
		java: OpenJDK 64-Bit Server VM 23
		lithium: Lithium 0.15.0+mc1.21.1
		minecraft: Minecraft 1.21.1
		modelfix: Model Gap Fix 1.21-1.6
		modmenu: Mod Menu 11.0.3
		nochatreports: No Chat Reports 1.21.1-v2.9.1
		openpartiesandclaims: Open Parties and Claims 0.25.8
		placeholder-api: Placeholder API 2.4.2+1.21
		pointblank: Point Blank: Jelly 2.1.0
		prickle: PrickleMC 21.1.11
		reeses-sodium-options: Reese's Sodium Options 1.8.3+mc1.21.4
		seedcrackerx: SeedCrackerX 2.15.0
		seedmapper: SeedMapper 2.0.0-beta.4+1.21
			betterconfig: BetterConfig 2.1.2
			clientarguments: clientarguments 1.9
		sodium: Sodium 0.6.13+mc1.21.1
		sodium-extra: Sodium Extra 0.6.0+mc1.21.1
		voicechat: Simple Voice Chat 1.21.1-2.6.6
			voicechat_api: Simple Voice Chat API 2.6.0
		xaerominimap: Xaero's Minimap 25.2.10
		xaeroworldmap: Xaero's World Map 1.39.12
	Loaded Shaderpack: (off)