package gitlet;

import static gitlet.StagingArea.*;
import static gitlet.Repository.*;


import java.nio.file.Paths;
import java.util.Map;
import java.util.Set;

public class Status {


    public static void getGlobalStatus() {
        StagingArea stagingArea = getStagingArea();

        System.out.println("=== Branches ===");
        getBranchesStatus();
        System.out.println();

        System.out.println("=== Staged Files ===");
        getStagedAddedStatus(stagingArea);
        System.out.println();
        System.out.println("=== Removed Files ===");
        getStagedRemovedStatus(stagingArea);
        System.out.println();

        System.out.println("=== Modifications Not Staged For Commit ===");
        System.out.println();

        System.out.println("=== Untracked Files ===");
        System.out.println();

    }

    private static void getBranchesStatus() {
        String[] branches = HEADSFOLDER.list();
        if (branches == null) {
            return;
        }
        for (String branch : branches) {
            if (getHeadBranchName().equals(branch)) {
                System.out.println("*" + branch);
            } else {
                System.out.println(branch);
            }
        }
    }

    private static void getStagedAddedStatus(StagingArea stagingArea) {
        Map<String, String> added = stagingArea.getAddedStagingFile();
        for (String filePath : added.keySet()) {
            System.out.println(Paths.get(filePath).getFileName().toString());
        }
    }

    private static void getStagedRemovedStatus(StagingArea stagingArea) {
        Set<String> removed = stagingArea.getRemovedStagingFile();
        for (String filePath : removed) {
            System.out.println(Paths.get(filePath).getFileName().toString());
        }
    }

    private static void getNotStagedStatus(StringBuilder status) {
    }

    private static void getNotTrackedStatus(StringBuilder status) {
    }

}
