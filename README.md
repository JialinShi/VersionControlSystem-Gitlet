# VersionControlSystem-Gitlet
Implemented a version-control system that mimics some features of the popular system Git.
A version-control system is essentially a backup system for related collections of files.

## The main functionality that Gitlet supports
1. Creates a new Gitlet version-control system in the current directory.
   
    `java gitlet.Main init`
3. Save the contents of entire directories of files. -add, remove, commit
    
    `java gitlet.Main add [fileName]`
   
    `java gitlet.Main rm [fileName]`
   
    `java gitlet.Main commit [message]`
4. Restore a version of one or more files or entire commits. -checkout
    - To checkout a file `java gitlet.Main checkout -- [fileName]`
    - To checkout to a commit `java gitlet.Main checkout [commitId] -- [fileName]`
    - To checkout to a branch `java gitlet.Main checkout [branchName]`
5. View the history of the commit. -log
    `java gitlet.Main log`
6. View the history of global commit -global-log
    `java gitlet.Main global-log`
7. Find commits with specified message -find
    `java gitlet.Main find [message]`
8. Check the status -status
    `java gitlet.Main status`
9. Create a branch -branch
    `java gitlet.Main branch [branchName]`
10. Remove a branch -rm-branch
    `java gitlet.Main rm-branch [branchName]`
11. Merge changes made in on branch into another branch. -merge
    `java gitlet.Main merge [branchName]`

## Classes and Data Structures
### Main
This is the entry point to the gitlet program.  It takes in arguments from the command line and based on the command calls the corresponding command in `Repository` which will acutally execute the logic of the command. It also validates the arguments based on the command to ensure that enough arguments were passed in.
### Repository
This is where the main logic of the program. This class handles all of the actual gitlet commands by reading/writing from/to the correct file, setting up persistence, and additional error checking.
It is responsible for setting up all persistence within gitlet.

### Commit
This class represents a `Commit` that will be stored. Each commit has a commit id, its parent commit id, timestamp, and a Map of files it tracks. Map<filePath, BlobId>.

All `Commit` objects are serialized within the commit_folder. 
`implements Serializable`
### Blob
This class represents the contents of files. A single file might correspond to multiple blobs: each being tracked in a different commit.
Each `Blob` stores a blob id, byte[] content, and file source.
All `Blon` objects are serialized within the Blob_folder,
`implements Serializable`
### StagingArea
This class represents the temporary holding area where you prepare your changes before they are permanently saved in a commit. It keeps track of added and removed files. Once commit is made, stagingArea will be cleared.
### AdditionalUtils
This class contains helpful utility methods to read/write objects or `String` contents from/to files, as well as reporting error with messages.


## Persistence
    --CWD
        -- .gitlet 
            -- commits 
                --66 (sha1 Dir)
                    --jdkajsnfoeinaid (sha1 fileName)
                    --awewadnfoeindeaw (sha1 fileName)
                --78 (sha1 Dir)
            -- blobs 
                --23 (sha1 Dir)
                    --qdka32nfoeinaid (sha1 fileName)
                    --wwewadnewaddaw (sha1 fileName)
                --44 (sha1 Dir)
            -- refs 
            -- heads 
            -- index File
            -- HEAD File



