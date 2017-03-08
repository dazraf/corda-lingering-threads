package io.dazraf.reproducer

import com.google.common.net.HostAndPort
import net.corda.core.createDirectory
import net.corda.core.exists
import net.corda.core.failure
import net.corda.core.node.services.ServiceType
import net.corda.core.success
import net.corda.node.driver.PortAllocation
import net.corda.node.internal.Node
import net.corda.node.printBasicNodeInfo
import net.corda.node.services.User
import net.corda.node.services.config.ConfigHelper
import net.corda.node.services.config.FullNodeConfiguration
import net.corda.node.utilities.ANSIProgressObserver
import org.apache.logging.log4j.LogManager
import java.io.File
import java.lang.management.ManagementFactory
import java.util.concurrent.Executors
import java.util.concurrent.ScheduledExecutorService
import kotlin.system.exitProcess

/**
 * This class bootstraps a node using the corda-node apis
 * After the node starts running, it waits for the user to hit ENTER
 * At which point Node.stop() is invoked
 * For good measure we force the shutdown of the ExecutorService
 * This demonstrates
 */
class Runner {
  companion object {
    private val DEFAULT_PORT_ALLOCATOR = PortAllocation.Incremental(10000)
    private val EXECUTOR_SERVICE: ScheduledExecutorService = Executors.newScheduledThreadPool(2)
    private val LOG = LogManager.getLogger(Runner::class.java)

    @JvmStatic fun main(args: Array<String>) {
      Runner().run()
    }
  }

  val nodeName = "TestNode"
  val messagingEndPoint: HostAndPort = DEFAULT_PORT_ALLOCATOR.nextHostAndPort()
  val networkMapNodeName: String = nodeName
  val networkMapEndPoint: HostAndPort = messagingEndPoint
  val webEndPoint: HostAndPort = DEFAULT_PORT_ALLOCATOR.nextHostAndPort()
  val rpcUsers: List<User> = listOf()
  val rootDataDirectory: File = File("build")
  val extraAdvertisedServiceIds: List<ServiceType> = listOf()

  fun run() {
    val fnc = createFullNodeConfig()
    val node = runNode(fnc)
    println("Node started. To see the stack, in a separate terminal run \njstack -l ${getProcessPID()}")
    println("Press ENTER to shutdown")
    readLine()
    shutdown(node)
    println("Node stopped. Your process *should* now complete.")
    // needs this line
    // exitProcess(0)
  }

  private fun shutdown(node: Node) {
    node.stop()

    // I put this in to check that it's not an executor service thread causing this issue
    EXECUTOR_SERVICE.shutdownNow()
  }

  private fun runNode(fnc: FullNodeConfiguration): Node {
    val startTime = System.currentTimeMillis()
    val renderBasicInfoToConsole = false
    val node = fnc.createNode()
    node.start()
    node.networkMapRegistrationFuture.success {
      val elapsed = (System.currentTimeMillis() - startTime) / 10 / 100.0
      printBasicNodeInfo("${fnc.myLegalName} started up and registered in $elapsed sec")

      if (renderBasicInfoToConsole)
        ANSIProgressObserver(node.smm)
    } failure {
      LOG.error("Error during network map registration", it)
      throw RuntimeException("Error during network map registration", it)
    }
    EXECUTOR_SERVICE.submit {
      node.run()
    }
    return node
  }

  private fun createFullNodeConfig(): FullNodeConfiguration {
    rootDataDirectory.mkdir()
    val baseDirectory = File(rootDataDirectory, nodeName).absoluteFile.toPath()
    val configOverrides = buildConfigOverrides()

    val baseConfig = ConfigHelper.loadConfig(
      baseDirectory = baseDirectory,
      allowMissingConfig = true,
      configOverrides = configOverrides)

    val fnc = FullNodeConfiguration(baseDirectory, baseConfig)

    if (!fnc.baseDirectory.exists()) {
      fnc.baseDirectory.createDirectory()
    }

    return fnc
  }

  private fun buildConfigOverrides(): Map<String, Any> {
    return mapOf(
      "myLegalName" to nodeName,
      "webAddress" to webEndPoint.toString(),
      "artemisAddress" to messagingEndPoint.toString(),
      "extraAdvertisedServiceIds" to extraAdvertisedServiceIds.map { it.id }.joinToString(separator = ","),
      "rpcUsers" to rpcUsers.map {
        mapOf(
          "user" to it.username,
          "password" to it.password,
          "permissions" to it.permissions
        )
      },
      "networkMapService" to mapOf(
        "legalName" to networkMapNodeName,
        "address" to networkMapEndPoint.toString()
      )
    )
  }

  private fun getProcessPID() : Int {
    val runtime = ManagementFactory.getRuntimeMXBean()
    val jvm = runtime.javaClass.getDeclaredField("jvm")
    jvm.isAccessible = true
    val mgmt = jvm.get(runtime) as sun.management.VMManagement
    val pid_method = mgmt.javaClass.getDeclaredMethod("getProcessId")
    pid_method.isAccessible = true

    val pid = pid_method.invoke(mgmt) as Int
    return pid
  }
}

