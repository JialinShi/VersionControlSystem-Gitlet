package gitlet;


import java.io.File;
import java.io.Serializable;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import static gitlet.Utils.*;

public class StagingArea implements Serializable {

    /**
     * Map<filepath, sha1></>file in the staging area, and sha1 based on content
     */
    private final Map<String, String> addedStagingFile = new HashMap<>();

    /**
     * remove file path (key in the map) from staging area once committed
     */
    private final Set<String> removedStagingFile = new HashSet<>();

    /**
     * tracked files map with file path as key and SHA1 as value
     */
    private transient Map<String, String> tracked;

    public StagingArea(Map<String, String> tracked) {
        this.tracked = tracked;
    }

    public StagingArea() {
    }

    public void setTracked(Map<String, String> tracked) {
        this.tracked = tracked;
    }


    /**
     * save this instance to the file Index
     */
    public void saveStaging() {
        writeObject(Repository.INDEX, this);
    }

    /**
     * Get a StagingArea instance from the file index
     *
     * @return StagingArea instance
     */
    public static StagingArea fromFile() {
        return readObject(Repository.INDEX, StagingArea.class);
    }

    /**
     * Perform a commit. add everything from addedStagingFiles, and remove everything mentioned in
     * removedStagingFile
     *
     * @return return a new Map of <filePath, SHA1></>
     */

    public Map<String, String> commit() {
        if (!isClean()) {
            tracked.putAll(addedStagingFile);
            for (String filePath : removedStagingFile) {
                tracked.remove(filePath);
            }
            clear();
        }
        return tracked;
    }

    /**
     * Remove file from staging area and tracked Map
     *
     * @param file instance
     * @return ture if the file exists and got removed
     */
    public boolean remove(File file) {
        String filePath = file.getPath();
        String blobIdToRemove = addedStagingFile.remove(filePath);
        if (blobIdToRemove != null) {
            return true;
        }
        if (tracked.get(filePath) != null) {
            if (file.exists()) {
                file.delete();
            }
            return removedStagingFile.add(filePath);
        }
        return false;
    }

    /**
     * Add the file to Staging area if not in Tracked && newly staged
     *
     * @param file File instance
     * @return return true if the staging area is changed
     */
    public boolean add(File file) {
        String filePath = file.getPath();
        Blob blob = new Blob(file);
        String blobID = blob.getBlobSHA1();
        if (inRemoved(filePath, blobID, tracked, removedStagingFile)) {
            return true;
        }
        if (inTracked(filePath, blobID, tracked)) {
            return false;
        }
        return ifNew(filePath, blobID, addedStagingFile, blob);
    }

    /**
     * determine if we have committed the same file before
     *
     * @return return True if the file is not found in tracked
     * @params filePath, blobID, Map
     */
    private boolean inTracked(String filePath, String blobID, Map<String, String> trackedMap) {
        String trackBlobId = trackedMap.get(filePath);
        if (trackBlobId != null && trackBlobId.equals(blobID)) {
            return true;
        }
        return false;
    }

    private boolean inRemoved(String filePath, String newBlobId, Map<String, String> trackedMap,
        Set<String> removed) {
        if (removed.contains(filePath)) {
            String blobId = trackedMap.get(filePath);
            if (newBlobId.equals(blobId)) {
                removed.remove(filePath);
                return true;
            }
        }
        return false;
    }

    /**
     * determine if the file is new to the staging area
     *
     * @return true if the file is new
     */
    private boolean ifNew(String filePath, String blobID, Map<String, String> staged,
        Blob blob) {
        String prevBlobID = staged.put(filePath, blobID);
        if (prevBlobID != null && prevBlobID.equals(blobID)) {
            return false;
        }
        blob.saveBlob();
        return true;
    }


    /**
     * Tell if the staging area is clean, which means no file to add, to modified, or to remove
     *
     * @return return true if clean
     */
    public boolean isClean() {
        return addedStagingFile.isEmpty() && removedStagingFile.isEmpty();
    }

    /**
     * clear the staging area
     */
    public void clear() {
        addedStagingFile.clear();
        removedStagingFile.clear();
    }

    public Map<String, String> getAddedStagingFile() {
        return addedStagingFile;
    }

    public Set<String> getRemovedStagingFile() {
        return removedStagingFile;
    }

    public Map<String, String> getTracked() {
        return tracked;
    }
}
