/**
 * This script initializes the document library structure from a database with
 * default files. 
 * 
 * @author Iñaki Sainz
 */
import com.liferay.portlet.documentlibrary.model.*;
import com.liferay.portlet.documentlibrary.service.*;
import com.liferay.portal.util.PropsValues;
import com.liferay.portlet.documentlibrary.model.DLFolderConstants;
import com.liferay.portal.kernel.dao.shard.*;
import com.liferay.portal.kernel.dao.orm.*;
import com.liferay.portal.*;                                             
import com.liferay.portal.model.*;
import com.liferay.portal.service.*;
import com.liferay.portal.security.auth.*;
import java.io.File;

/* function for getting the shard id from the shard name */
long getCompanyIdByShardName(String shardName){
  List allShards = ShardLocalServiceUtil.getShards(QueryUtil.ALL_POS, QueryUtil.ALL_POS);
	long result = 0;
	if (allShards!=null){
		for (i=0;i<allShards.size(); i++){
			if (allShards.get(i).getName().equals(shardName)){
				return allShards.get(i).getClassPK();
			}
		}
	}

	return result;
}

List<String> toCreate = new ArrayList<String>();

/* PATH A LA DOCUMENT LIBRARY*/
String cleanDLRootPath = "/Proyectos/vass-telefonica-tienda/staging-test/liferay-portal-6.2-ee-sp5/data/document_library";

/* Default files */
String defaultPNG = "/Proyectos/vass-telefonica-tienda/staging-test/default.png";
String defaultJPG = "/Proyectos/vass-telefonica-tienda/staging-test/default.jpg";
String defaultGIF = "/Proyectos/vass-telefonica-tienda/staging-test/default.gif";
String defaultCSS = "/Proyectos/vass-telefonica-tienda/staging-test/default.css";
String defaultJS = "/Proyectos/vass-telefonica-tienda/staging-test/default.js";
String defaultZIP = "/Proyectos/vass-telefonica-tienda/staging-test/default.zip";
String defaultLAR = "/Proyectos/vass-telefonica-tienda/staging-test/default.lar";
String defaultSWF = "/Proyectos/vass-telefonica-tienda/staging-test/default.swf";
String defaultPDF = "/Proyectos/vass-telefonica-tienda/staging-test/default.pdf";

/* OS version */
String osName = System.getProperty("os.name").toLowerCase();


String[] shardNames = ShardUtil.getAvailableShardNames();

for (String shardName : shardNames) {
  
    /* Setting shardname for queries */
    long companyId = getCompanyIdByShardName(shardName);
    
    ServiceContext ctx = new ServiceContext();
    ctx.setCompanyId(companyId);
    out.println ("Processing SHARD " + shardName + " AND COMPANY" + companyId);

    try{
        /* selecting the right shard */
        ShardUtil.pushCompanyService(companyId);

        List<DLFileEntry> dlFileEntries = DLFileEntryLocalServiceUtil.getFileEntries(-1, -1);

        out.println ("Checking " + dlFileEntries.size() + " files");

        for (DLFileEntry fileEntry : dlFileEntries) {

             String rootFolder = fileEntry.getCompanyId();
             String subFolder = fileEntry.getName();
             String dataRepositoryId = DLFolderConstants.getDataRepositoryId(fileEntry.getRepositoryId(), fileEntry.getFolderId());
             String dirPath = rootFolder + "/" + dataRepositoryId + "/" + subFolder;
             String folder = PropsValues.DL_STORE_FILE_SYSTEM_ROOT_DIR + "/" + dirPath  ;

             for(DLFileVersion version: DLFileVersionLocalServiceUtil.getFileVersions(fileEntry.getFileEntryId(), -1)) {
                 String versionPath = dirPath + "/" + version.getVersion();
                 String versionFullPath = folder + "/" +  version.getVersion();
              
                File file = new File(versionFullPath);
                if (!file.exists()) {
                      String creationCmd = "";
                      /* MAC doesn't suppot --parents flag of cp command */
                      if (osName.indexOf("mac") >= 0){
                        creationCmd = "mkdir -p " + dirPath + ";";
                      }
                  
                      creationCmd = creationCmd.concat(" cp ")
                      
                      if (osName.indexOf("mac") < 0){
                        creationCmd = creationCmd.concat("--parents ")
                      }
                      
                      if (version.getExtension().equals("jpg")) { creationCmd = creationCmd.concat(defaultJPG) }
                      else if (version.getExtension().equals("png")) { creationCmd = creationCmd.concat(defaultPNG) }
                      else if (version.getExtension().equals("gif")) { creationCmd = creationCmd.concat(defaultGIF) }
                      else if (version.getExtension().equals("js")) { creationCmd = creationCmd.concat(defaultJS) }
                      else if (version.getExtension().equals("css")) { creationCmd = creationCmd.concat(defaultCSS) }
                      else if (version.getExtension().equals("zip")) { creationCmd = creationCmd.concat(defaultZIP) }
                      else if (version.getExtension().equals("lar")) { creationCmd = creationCmd.concat(defaultLAR) }
                      else if (version.getExtension().equals("swf")) { creationCmd = creationCmd.concat(defaultSWF) }
                      else if (version.getExtension().equals("pdf")) { creationCmd = creationCmd.concat(defaultPDF) }
                      else {
                        out.println ("WARNING Default file for extension " + version.getExtension() + " not found ");
                      }
                      
                      creationCmd = creationCmd.concat(" " + versionFullPath + ";");
                      toCreate.add(creationCmd);
                } else {
                        out.println ("WARNING File found for fileEnryVersion " + version + " in " + versionPath);
                }
             }
        }
    }
    catch (Exception e){    
        e.printStackTrace();
    }
    finally {
        ShardUtil.popCompanyService();
    }
}

out.println("---------------------------------");
out.println(toCreate.size() + " versions found" );
out.println ("Current DL PATH: "+ PropsValues.DL_STORE_FILE_SYSTEM_ROOT_DIR );
out.println("Please execute following commands to create a clean dl in: " + cleanDLRootPath)

out.println("<textarea style=\"width:1024px;min-height:600px\">");

out.println ("cd " +PropsValues.DL_STORE_FILE_SYSTEM_ROOT_DIR + "/ ; " ); 
if ( !(new File(cleanDLRootPath)).exists() ) {
	out.println ("mkdir --parents " + cleanDLRootPath + ";");
}

for (String current : toCreate) {
  out.println(current);
}

out.println("</textarea>");

