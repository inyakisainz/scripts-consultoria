import java.util.List
import java.lang.management.ManagementFactory 
import java.lang.management.MemoryPoolMXBean
import java.lang.management.RuntimeMXBean

import com.liferay.portal.util.PortalUtil
import com.liferay.portal.kernel.cluster.ClusterExecutorUtil
import com.liferay.portal.kernel.cluster.Address
import com.liferay.portal.kernel.util.PropsUtil
import com.liferay.portal.kernel.util.ReleaseInfo
import com.liferay.portal.kernel.util.PropertiesUtil

import com.liferay.portlet.journal.service.JournalTemplateLocalServiceUtil
import com.liferay.portlet.journal.model.JournalTemplate


println("Init diagnostics")
println("Diagnostics time" + new java.util.Date())
println("Uptime: " + PortalUtil.getUptime())
println("ComputerName: " + PortalUtil.getComputerName())
println("ComputerAddress: " + PortalUtil.getComputerAddress())
println("Liferay Release: " + ReleaseInfo.getReleaseInfo())
println("Number of cores: " + Runtime.getRuntime().availableProcessors())
println("Max memory: " + Runtime.getRuntime().maxMemory())
println("Total memory: " + Runtime.getRuntime().totalMemory())

println("==================================================")
println("JDK Enviroment description")
println("==================================================")
println("JDK: ")
RuntimeMXBean re =  ManagementFactory.getRuntimeMXBean();
println(re.getVmName() + " " + re.getVmVendor() + " " + re.getVmVersion());
println("");
println("Java Arguments: ")
for (String arguments : ManagementFactory.getRuntimeMXBean().getInputArguments()) {
    println(arguments)
}
println("==================================================")

println("==================================================")
println("Operating System Enviroment description")
println("==================================================")



println("==================================================")

println("==================================================")
println("System Enviroment variables")
println("==================================================")
envProperties = System.getenv()
for (envKey in envProperties.keySet()) {
println(envKey + "=" + envProperties.get(envKey) )
}
println("==================================================")

println("==================================================")
println("System Properties variables")
println("==================================================")
systemProp = System.getProperties()
for (envKey in systemProp.keySet()) {
println(envKey + "=" + systemProp.getProperty(envKey))
}
println("==================================================")

println("==================================================")
println("Portal Properties")
println("==================================================")

result = PropertiesUtil.load(getClass().getClassLoader().getResourceAsStream("portal.properties"),"UTF-8")

systemProp = PropsUtil.getProperties()
for (envKey in systemProp.keySet()) {
if (!result.getProperty(envKey).equals(systemProp.getProperty(envKey))) {
print("<font style='color:red'>**</font>")
}
println(envKey + "=" + systemProp.getProperty(envKey))
}
println("==================================================")

println("==================================================")
println("Cluster configuration")
println("==================================================")
try {
    List<Address> nodes = ClusterExecutorUtil.getClusterNodeAddresses();

    println "Clustering enabled:" + ClusterExecutorUtil.isEnabled()
    println "Clustered nodes:" + nodes.size()

    for (Address node : nodes) {
        print (node.getRealAddress());
        print (", is alive? ")
                println( ClusterExecutorUtil.isClusterNodeAlive(node));

    }
}
catch (Exception e) {
    println e
}
println("==================================================")

println("==================================================")
println("Journal Template cacheable")
println("==================================================")
try {
    List<JournalTemplate> temlates = JournalTemplateLocalServiceUtil.getTemplates()

    println "Number of templates=" + temlates.size()

    for (JournalTemplate template : temlates) {
        if (!template.isCachedModel())
        println("Template " + template.getName() + " (id=" + template.getId() + ") is not cacheable")
    }
}
catch (Exception e) {
    println e
}
println("==================================================")


println("End diagnostics")