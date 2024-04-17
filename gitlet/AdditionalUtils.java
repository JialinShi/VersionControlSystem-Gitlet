package gitlet;


import static gitlet.Utils.join;
import static gitlet.Utils.writeObject;

import java.io.File;
import java.io.Serializable;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;


import static gitlet.Repository.*;

public class AdditionalUtils {

    /**
     * Print out Message and exits with error code -1.
     *
     * @param message - message to print
     */
    public static void exit(String message) {
        System.out.println(message);
        System.exit(0);
    }

    /**
     * Get directory name from Sha1 id in the objects folder.
     *
     * @param sha1 Sha1 id
     * @return Name of the directory
     */
    public static String getShaDir(String sha1) {
        return sha1.substring(0, 2);
    }

    public static String getShaFileName(String sha1) {
        return sha1.substring(2);
    }

    public static File getObjectFile(String commitSha1) {
        String dir = getShaDir(commitSha1);
        String fileName = getShaFileName(commitSha1);
        File returnFile = join(COMMITFOLDER, dir, fileName);
        return returnFile;
    }

    public static File getBlobFile(String blobSha1) {
        String dir = getShaDir(blobSha1);
        String fileName = getShaFileName(blobSha1);
        File returnFile = join(BLOBFOLDER, dir, fileName);
        return returnFile;
    }

    public static void saveObject(File file, Serializable object) {
        File dir = file.getParentFile();
        if (!dir.exists()) {
            dir.mkdir();
        }
        writeObject(file, object);
    }


    private static final SimpleDateFormat FORMATTER = new SimpleDateFormat(
        "EEE MMM d HH:mm:ss yyyy Z");

    public static Date parseTimestamp(String timestamp) {
        try {
            return FORMATTER.parse(timestamp);
        } catch (ParseException e) {
            throw new IllegalArgumentException("Invalid date format", e);
        }
    }


}
