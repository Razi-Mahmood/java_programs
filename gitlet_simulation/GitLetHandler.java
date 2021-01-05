package gitlet;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.TimeZone;

public class GitLetHandler {
	public static TimeZone DEFAULT_TIMEZONE = TimeZone.getTimeZone("PST");
	private HeadInfo hinfo;

	private HashMap<String, String> branchMap;
	private CommitTree commitMap;
	private HashMap<String, String> stagingMap;
	private String blobdir = "blobs";
	private String stagingdir = "staging";
	private String markedloc;
	private HashSet<String> markedSet = null;
	private String commitMessage = null;
	private String headloc, commitloc, blobloc, stagingmaploc, branchloc;
	private String currdir;
	public GitLetHandler() {
		currdir = System.getProperty("user.dir");
	}
	HashMap<String, String> copyBlobs(HashMap<String, String> incoming) {
		if (incoming == null) {
			return null;
		}
		HashMap<String, String> out = new HashMap<>();
		Iterator it = incoming.keySet().iterator();
		String key, val;
		while (it.hasNext()) {
			key = (String) it.next();
			val = incoming.get(key);
			if (markedSet == null) {
				out.put(key, val);
			} else if (!markedSet.contains(key)) {
				out.put(key, val);
			} else {
				markedSet.remove(key);
			}
		}
		return out;
	}

	/**
	 * @param message message
	 * @param branch branch
	 * @return commit
	 */
	Commit createNewCommit(String message, String branch) {
		Commit newcom = new Commit();
		newcom.message = message;
		newcom.time = new Date();
		newcom.branch = branch;
		String branchcode = getBranchCode(branch);
	    CommitTree existtree = locateBranch(branchcode, commitMap);
	 
		Commit existcom = existtree.com;
		newcom.blobs = copyBlobs(existcom.blobs);
		if (stagingMap != null) {
			String origfilename;
			String hashcode;
			Iterator it = stagingMap.keySet().iterator();
			while (it.hasNext()) {
				origfilename = (String) it.next();
				hashcode = stagingMap.get(origfilename);
				if (newcom.blobs == null) {
					newcom.blobs = new HashMap<>();
				}
				newcom.blobs.put(origfilename, hashcode);
			}
		}
		Utils.restrictedDelete(stagingmaploc);
		return newcom;
	}

	/**
	 * @param newcom
	 * @param hashcode
	 * @param existtree
	 */
	void adjustPointers(Commit newcom,String hashcode,CommitTree existtree)
	{
		CommitTree tree=new CommitTree();
		if (tree.parents==null) {
			tree.parents=new ArrayList<CommitTree>();
		}
		tree.parents.add(existtree);
		tree.com=newcom;
		tree.code=hashcode;
		if (existtree.children==null) {
			existtree.children=new ArrayList();
		}
		existtree.children.add(tree);
		 branchMap.put(hinfo.currbranchname, hashcode);
		hinfo.headcode=hashcode; //replace the head pointer to this hashcode
	}
	/**
	 * @param currbranch
	 * @param currtree
	 * @param giventree
	 * @param newblobMap
	 */
//	public void doMergeCommit(String currbranch,
//			CommitTree currtree, CommitTree giventree,
//							  HashMap<String, String> newblobMap) {
//		Commit newcom = new Commit();
//		newcom.message = commitMessage;
//		newcom.time = new Date();
//		newcom.branch = currbranch;
//		newcom.blobs = newblobMap;
//		if (stagingMap != null)	{
//			String origfilename;
//			String hashcode;
//			Iterator it = stagingMap.keySet().iterator();
//			while (it.hasNext()) {
//				origfilename = (String) it.next();
//				hashcode = stagingMap.get(origfilename);
//				if (newcom.blobs == null) {
//					newcom.blobs = new HashMap<>();
//				}
//				newcom.blobs.put(origfilename, hashcode);
//			}
//		}
//		String hashcode = generateShCode(Utils.serialize(newcom));
//		CommitTree tree = new CommitTree();
//		tree.parents = new ArrayList<CommitTree>();
//		tree.parents.add(currtree);
//		tree.parents.add(giventree);
//		if (currtree.children == null) {
//			currtree.children = new ArrayList();
//		}
//		currtree.children.add(tree);
//		if (giventree.children == null) {
//			giventree.children = new ArrayList<CommitTree>();
//		}
//		giventree.children.add(tree);
//		tree.com = newcom;
//		tree.code = hashcode;
//		commitMap = tree;
//		branchMap.put(hinfo.currbranchname, hashcode);
//		hinfo.headcode = hashcode;
//		Utils.writeObject(new File(commitloc), commitMap);
//		Utils.writeObject(new File(headloc), hinfo);
//		Utils.writeObject(new File(branchloc), branchMap);
//		Utils.writeObject(new File(markedloc), markedSet);
//		Utils.restrictedDelete(stagingmaploc);
//	}


	public void doMergeCommit(String currbranch,
							  CommitTree currtree, CommitTree giventree,
							  HashMap<String, String> newblobMap) {
		Commit newcom = new Commit();
		newcom.message = commitMessage;
		newcom.time = new Date();
		newcom.branch = currbranch;
		newcom.blobs = newblobMap;
		if (stagingMap != null) {
			String origfilename;
			String hashcode;
			Iterator it = stagingMap.keySet().iterator();
			while (it.hasNext()) {
				origfilename = (String) it.next();
				hashcode = stagingMap.get(origfilename);
				if (newcom.blobs == null) {
					newcom.blobs = new HashMap<>();
				}
				newcom.blobs.put(origfilename, hashcode);
			}
		}
		String hashcode = generateShCode(Utils.serialize(newcom));
		CommitTree tree = new CommitTree();
		tree.parents = new ArrayList<CommitTree>();
		tree.parents.add(currtree);
		tree.parents.add(giventree);
		if (currtree.children == null) {
			currtree.children = new ArrayList();
		}
		currtree.children.add(tree);
		if (giventree.children == null) {
			giventree.children = new ArrayList<CommitTree>();
		}
		giventree.children.add(tree);
		tree.com = newcom;
		tree.code = hashcode;
//commitMap = tree;
		branchMap.put(hinfo.currbranchname, hashcode);
		hinfo.headcode = hashcode;
		Utils.writeObject(new File(commitloc), commitMap);
		Utils.writeObject(new File(headloc), hinfo);
		Utils.writeObject(new File(branchloc), branchMap);
		Utils.writeObject(new File(markedloc), markedSet);
		Utils.restrictedDelete(stagingmaploc);
	}




	/**MESSAGE.*/
	public void doCommit(String message) {
		setup();
		if (stagingMap == null && markedSet == null) {
			throw new GitletException("No changes added to the commit.");
		} else if ((message == null) || (message.trim().equals(""))) {
			throw new GitletException("Please enter a commit message.");
		} else {
			Commit newcom = createNewCommit(message, hinfo.currbranchname);
			String hashcode = generateShCode(Utils.serialize(newcom));
			String head = getBranchCode(hinfo.currbranchname);
			adjustPointers(newcom, hashcode, locateBranch(head,commitMap));
			Utils.writeObject(new File(commitloc), commitMap);
			Utils.writeObject(new File(headloc), hinfo);
			Utils.writeObject(new File(branchloc), branchMap);
			Utils.writeObject(new File(markedloc), markedSet);
		}
	}
	
	public void doBranch(String branchname) {
		setup();
		if (branchNameExists(branchname)) {
			throw new GitletException("A branch with that name already exists.");
		} else if (hinfo != null) {
			if (branchMap == null) {
				branchMap = new HashMap<>();
			}
			String head = getBranchCode(hinfo.currbranchname);
			branchMap.put(branchname, head);
			Utils.writeObject(new File(branchloc), branchMap);
		}
	}
	String[] sortedNames(HashMap<String, String> stringMap) {
		if (stringMap == null) {
			return null;
		}
		Iterator it = stringMap.keySet().iterator();
		String stringname;

		ArrayList<String> names = null;
		while (it.hasNext()) {
			stringname = (String) it.next();
			if (names == null) {
				names = new ArrayList<>();
			}
			names.add(stringname);
		}
		if (names != null) {
			String[] namesarray = (String[]) names.toArray(new String[names.size()]);
			Arrays.sort(namesarray);
			return namesarray;
		}
		return null;
	}

	String[] sortedNamesInHashSet(HashSet<String> stringSet) {
		if (stringSet == null) {
			return null;
		}
		Iterator it = stringSet.iterator();
		String stringname;
		ArrayList<String> names = null;
		while (it.hasNext()) {
			stringname = (String) it.next();
			if (names == null) {
				names = new ArrayList<>();
			}
			names.add(stringname);
		}
		if (names != null) {
			String[] namesarray = (String[]) names.toArray(
									new String[names.size()]);
			Arrays.sort(namesarray);
			return namesarray;
		}
		return null;
	}

	void printBranchMap(HashMap<String, String> branchMap) {
		if (branchMap == null) {
			return;
		}
		String[] namesarray = sortedNames(branchMap);
		if (namesarray != null) {
			System.out.println("=== Branches ===");
			String branchname;
			for (int i = 0; i < namesarray.length; i++) {
				branchname = namesarray[i];
				if (branchname.equals("master")) {
					System.out.println("*" + branchname);
				} else {
					System.out.println(branchname);
				}
			}
			System.out.println();
		}
	}

	void printStagingMap(HashMap<String, String> stagingMap) {
		if (stagingMap == null) {
			System.out.println("=== Staged Files ===");
			System.out.println();
		}
		String[] namesarray = sortedNames(stagingMap);
		if (namesarray != null) {
			System.out.println("=== Staged Files ===");
			String stagingname;
			for (int i = 0; i < namesarray.length; i++) {
				stagingname = namesarray[i];
				System.out.println(stagingname);
			}
			System.out.println();
		}
	}
	void printMarkedSet(HashSet<String> markedSet) {
		System.out.println("=== Removed Files ===");
		if (markedSet == null || markedSet.size() == 0) {
			System.out.println();
		} else {
			String[] namesarray = sortedNamesInHashSet(markedSet);
			if (namesarray != null) {
				String markedname;
				for (int i = 0; i < namesarray.length; i++) {
					markedname = namesarray[i];
					System.out.println(markedname);
				}
				System.out.println();
			}
		}
	}

	boolean branchNameExists(String name) {
		if (branchMap == null) {
			return false;
		}
		return (branchMap.get(name) != null);
	}

	String getBranchCode(String name) {
		if (branchMap == null) {
			return null;
		}
		return branchMap.get(name);
	}

	public void doCheckoutFile(String filename) {
		setup();
		if (commitMap != null) {
			String head = getBranchCode(hinfo.currbranchname);
			CommitTree cmt = locateBranch(head, commitMap);
			handleFileCheckout(filename, cmt);
		}
	}

	public void doCheckoutBranch(String branch) {
		setup();
		if (branchNameExists(branch)) {
			handleBranchCheckout(branch);
		} else {
			throw new GitletException("No such branch exists.");
		}
	}

	public void doCheckoutCommitCode(String commitcode, String filename) {
		setup();
		handleCommitCodeCheckout(commitcode,filename);
	}

	public void doMerge(String givenbranchname) {
		setup();
		if ((stagingMap != null) || (markedSet != null)) {
			throw new GitletException("You have uncommitted changes.");
		} else {
			String givenbranchcode = getBranchCode(givenbranchname);
			if (givenbranchcode == null) {
				throw new GitletException("A branch with that name does not exist.");
			} else if (hinfo.currbranchname.equals(givenbranchname)){
				throw new GitletException("Cannot merge a branch with itself");
			} else {
				String currentbranchcode = hinfo.headcode;
				CommitTree currentcmt = locateBranch(currentbranchcode, commitMap);
				CommitTree givencmt = locateBranch(givenbranchcode, commitMap);
				if (untrackedFileGivenBranch(currentcmt.com.blobs, givencmt.com.blobs)) {
					throw new GitletException("There is an untracked file in the way; delete it or add it first.");
				} else {
					handleMerge(hinfo.currbranchname, givenbranchname, currentbranchcode,
							givenbranchcode, currentcmt, givencmt);
				}
			}
		}
	}

	public void doReset(String commitcode) {
		setup();
		if (commitcode != null) {
			CommitTree cmt=locateCommit(commitcode,commitMap);
			if (cmt == null) {
				throw new GitletException("No commit with that id exists.");
			} else {
				handleReset(cmt);
			}
		}
	}
	void removeAllTrackedFiles(HashMap<String, String> blobMap) {
		String head = getBranchCode(hinfo.currbranchname);
		CommitTree currbranch = locateBranch(head,commitMap);
		HashMap<String, String> currblobMap = currbranch.com.blobs;

		if (currblobMap != null) {
		   Iterator it = currblobMap.keySet().iterator();
		   String filename;
		   while (it.hasNext()) {
			   filename = (String) it.next();
			   if (blobMap.get(filename) == null) {
				   Utils.restrictedDelete(filename);
			   }
		   }
	   	}
		if (stagingMap != null) {
			Iterator it = stagingMap.keySet().iterator();
			String filename;
			while (it.hasNext()) {
				filename = (String) it.next();
				Utils.restrictedDelete(filename);
			}
		}
	}
	void handleReset(CommitTree cmt) {
		if (cmt != null) {
			HashMap<String, String> blobMap = cmt.com.blobs;
			removeAllTrackedFiles(blobMap);
			if (blobMap != null) {
			   Iterator it = blobMap.keySet().iterator();
			   String filename;
			   while (it.hasNext()) {
			   		filename = (String) it.next();
			   		overwriteWorkingDirectoryFile(filename, blobMap);
			   }
			}
			Utils.restrictedDelete(stagingmaploc);
			hinfo.currbranchname = cmt.com.branch;
			hinfo.headcode = getBranchCode(hinfo.currbranchname);
			Utils.writeObject(new File(headloc), hinfo);
		}
	}

	ArrayList<String> allPaths;
	String getAncestorsPath(CommitTree cmt, String startcode) {
		String path = "";
		if (cmt != null) {
			String currbranchcode = cmt.code;
			if (cmt.parents != null) {
				for (int i = 0; i < cmt.parents.size(); i++) {
					path = currbranchcode + "\t" + getAncestorsPath(cmt.parents.get(i), startcode);
					if (path.startsWith(startcode)) {
						if (allPaths == null) {
							allPaths = new ArrayList<>();
						}
						allPaths.add(path);
					}
				}
			} else {
				path = currbranchcode;
			}
			return path;
		} else {
			return path;
		}
	}
	/** MAINCODES GIVENCODES RETURN. */
	int positionInCurrentBranch(String[] mainCodes, String[] givenCodes) {
		String code;
		if (mainCodes.length < givenCodes.length) {
			HashSet<String> existings = new HashSet<>();
			for (int i = 0; i < givenCodes.length; i++) {
				existings.add(givenCodes[i]);
			}
			boolean found = false;
			int i = 0;
			while ((!found) && (i < mainCodes.length)) {
				code = mainCodes[i];
				if (existings.contains(code)) {
					found = true;
				} else {
					i++;
				}
			}
			return i;
		} else {
			HashSet<String> existings = new HashSet<>();
			for (int i = 0; i < mainCodes.length; i++) {
				existings.add(mainCodes[i]);
			}
			boolean found = false;
			int i = 0;
			while ((!found) && (i < givenCodes.length)) {
				code = givenCodes[i];
				if (existings.contains(code)) {
					found = true;
				} else {
					i++;
				}
			}
			return i;
		}
	}
	String findShortestAncestor(ArrayList<String> mainancestors,
								ArrayList<String> givenancestors) {
		String[] mainCodes, givenCodes;
		int smallestpos = Integer.MAX_VALUE;
		int pos;
		String assocCode = null;
		for (int i = 0; i < mainancestors.size(); i++) {
			mainCodes = mainancestors.get(i).split("\t");
			for (int j = 0; j < givenancestors.size(); j++) {
				givenCodes = givenancestors.get(j).split("\t");
				pos = positionInCurrentBranch(mainCodes, givenCodes);
				if (pos < smallestpos) {
					smallestpos = pos;
					assocCode = mainCodes[smallestpos];
				}
			}
		}
		return assocCode;
	}
	String findLeastCommonAncestor(CommitTree maincmt, CommitTree givencmt) {
		allPaths = null;
		getAncestorsPath(maincmt, maincmt.code);
		ArrayList<String> mainancestors = new ArrayList<>();
		for (int i = 0; i < allPaths.size(); i++) {
			mainancestors.add(allPaths.get(i));
		}
		allPaths = null;
		getAncestorsPath(givencmt, givencmt.code);
		ArrayList<String> givenancestors = new ArrayList<>();
		for (int i = 0; i < allPaths.size(); i++) {
			givenancestors.add(allPaths.get(i));
		}
		return findShortestAncestor(mainancestors, givenancestors);
	}
	boolean modifiedInSameWay(String code1, String code2) {
		if ((code1 == null) && (code2 == null)) {
			return true;
		} else if (code1.equals(code2)) {
			return true;
		} else {
			return false;
		}
	}
	boolean modified(String code1, String code2) {
		if ((code1 == null) && (code2 == null)) {
			return false;
		} else if ((code1 == null) && (code2 != null)) {
			return true;
		} else if ((code1 != null) && (code2 == null)) {
			return true;
		} else {
			return (!(code1.equals(code2)));
		}
	}
	/** CURRENTBLOBS GIVENBLOBS RETURN. */
	HashSet<String> inEitherBranches(HashMap<String, String> currentBlobs,
								HashMap<String, String> givenBlobs) {
		HashSet<String> keys = null;
		Iterator it = currentBlobs.keySet().iterator();
		String filename;
		while (it.hasNext()) {
			filename = (String) it.next();
			if (keys == null) {
				keys = new HashSet<>();
			}
			keys.add(filename);
		}
		it = givenBlobs.keySet().iterator();
		while (it.hasNext()) {
			filename = (String) it.next();
			if (keys == null) {
				keys = new HashSet<>();
			}
			keys.add(filename);
		}
		return keys;
	}
	/**
	 * @param currentbranchname a name
	 * @param givenbranchname another name
	 * @param current current tree
	 * @param lca common ancestor tree
	 * @param given the given tree
	 * @return a hashmap
	 */
	HashMap<String, String> handleFileMerges(String currentbranchname,
											 String givenbranchname,
											 CommitTree current,
											 CommitTree lca,
											 CommitTree given) {
		HashMap<String, String> currentBlobs = current.com.blobs;
		HashMap<String, String> lcBlobs = lca.com.blobs;
		HashMap<String, String> givenBlobs = given.com.blobs;
		HashSet<String> keys = inEitherBranches(currentBlobs, givenBlobs);
		if (keys == null) {
			return null;
		}
		Iterator it = keys.iterator();
		String filename, givenblobcode, lcblobcode, currentblobcode;
		HashMap<String, String> newblobMap = copyBlobs(currentBlobs);
		commitMessage = "Merged " + givenbranchname + " into " + currentbranchname;
		while (it.hasNext()) {
			filename = (String) it.next();
			givenblobcode = givenBlobs.get(filename);
			lcblobcode = lcBlobs.get(filename);
			currentblobcode = currentBlobs.get(filename);
			if (modified(givenblobcode, lcblobcode)) {
				if (!modified(currentblobcode, lcblobcode)) {
					if (givenblobcode != null) {
						if (stagingMap == null) {
							stagingMap = new HashMap<>();
						}
						stagingMap.put(filename, givenblobcode);
					}
				}
			}
			if ((lcblobcode == null) && (givenblobcode != null) && (currentblobcode == null)) {
				if (stagingMap == null) {
					stagingMap = new HashMap<>();
				}
				stagingMap.put(filename, givenblobcode);
			}
			if ((lcblobcode != null) && (givenblobcode == null)
					&& (!modified(currentblobcode, lcblobcode))) {
				if (markedSet == null) {
					markedSet = new HashSet<>();
				}
				markedSet.add(filename);
				removeWorkingDirectoryFile(filename);
				if ((stagingMap != null)
						&& (stagingMap.containsKey(filename))) {
					stagingMap.remove(filename);
				}
				if ((newblobMap != null)
						&& (newblobMap.containsKey(filename))) {
					newblobMap.remove(filename);
				}
			}
			if (!modifiedInSameWay(currentblobcode, givenblobcode)) {
				String newcode = mergeFiles(filename, currentblobcode, givenblobcode);
				if (stagingMap == null) {
					stagingMap = new HashMap<>();
				}
				stagingMap.put(filename, newcode);
				commitMessage = "Encountered a merge conflict.";
			}
		}
		return newblobMap;
	}
	/** FILENAME CURRENTBLOBCODE GIVENBLOBCODE RETURN. */
	String mergeFiles(String filename, String currentblobcode,
					  String givenblobcode) {
		String contentCurrent = "";
		String contentGiven = "";
		if (currentblobcode != null) {
			String blobfile = currdir + "/.gitlet/blobs/" + currentblobcode;
			if (new File(blobfile).exists()) {
				contentCurrent = Utils.readContentsAsString(new File(blobfile));
			}
		}
		if (givenblobcode != null) {
			String blobfile = currdir + "/.gitlet/blobs/" + givenblobcode;
			if (new File(blobfile).exists()) {
				contentGiven = Utils.readContentsAsString(new File(blobfile));
			}
		}
		String line1 = "<<<<<<< HEAD";
		String line2 = "=======";
		String line3 = ">>>>>>>";
		String alllines = line1 + contentCurrent + line2 + contentGiven + line3;
		Utils.writeContents(new File(filename), alllines);
		String hashcode = generateShCode(Utils.serialize(alllines));
		File blobdir = new File(blobloc);
		if (!blobdir.exists()) {
			blobdir.mkdir();
		}
		File blobfile = new File(blobdir.getAbsolutePath() + "/" + hashcode);
		Utils.writeContents(blobfile, alllines);
		return hashcode;
	}
	/** CURRENTBRANCH GIVENBRANCH CURRENTBRANCHCODE
	 * GIVENBRANCHCODE CURRENTCMT GIVENCMT. */
	void handleMerge(String currentbranch, String givenbranch,
					 String currentbranchcode,
					 String givenbranchcode, CommitTree currentcmt,
					 CommitTree givencmt) {
		String lccode = findLeastCommonAncestor(currentcmt, givencmt);
		CommitTree lcnode = locateCommit(lccode, commitMap);
		if (lcnode != null) {
			if (givencmt.code.equals(lcnode)) {
				System.out.println("Given branch is an "
						+ "ancestor of the current branch.");
			} else if (currentcmt.com.equals(lcnode.com)) {
				hinfo.headcode = givencmt.code;
				Utils.writeObject(new File(headloc), hinfo);
				System.out.println("Current branch fast-forwarded.");
			} else {
				HashMap<String, String> newblobMap =
						handleFileMerges(currentbranch, givenbranch,
								currentcmt, lcnode, givencmt);
				doMergeCommit(currentbranch, currentcmt, givencmt, newblobMap);
			}
		}
	}
	/** FILENAME FILEMAP RETURN. */
	boolean tracked(String filename, HashMap<String, String> fileMap) {
		if (fileMap == null) {
			return false;
		}
		boolean found = false;
		Iterator it = fileMap.keySet().iterator();
		String name;
		while ((!found) && (it.hasNext())) {
			name = (String) it.next();
			if (name.equals(filename)) {
				found = true;
			}
		}
		return found;
	}
	/** FILENAME RETURN. */
	boolean trackedInCommit(String filename) {
		String head = getBranchCode(hinfo.currbranchname);
		CommitTree cmt = locateBranch(head, commitMap);
		HashMap<String, String> blobMap = cmt.com.blobs;
		return (tracked(filename, blobMap));
	}
	/** FILENAME RETURN. */
	boolean untrackedFile(String filename) {
		String head = getBranchCode(hinfo.currbranchname);
		CommitTree cmt = locateBranch(head, commitMap);
		HashMap<String, String> blobMap = cmt.com.blobs;
		return (!((tracked(filename, stagingMap))
				|| (tracked(filename, blobMap))));
	}
	/** RETURN. */
	boolean untrackedFile() {
		File[] files = new File(currdir).listFiles();
		boolean found = false;
		int i = 0;
		String filename;
		String head = getBranchCode(hinfo.currbranchname);
		CommitTree cmt = locateBranch(head, commitMap);
		HashMap<String, String> blobMap = cmt.com.blobs;

		while ((!found) && (i < files.length)) {
			if (!files[i].isDirectory()) {
				filename = files[i].getName();
				if (!((filename.endsWith(".zip")) || (filename.endsWith(".jar"))
						|| (filename.endsWith(".DS_Store"))
						|| (filename.endsWith(".classpath"))
						|| (filename.endsWith(".project")))) {
					if (!((tracked(filename, stagingMap))
							|| (tracked(filename, blobMap)))) {
						found = true;
					} else {
						i++;
					}
				} else {
					i++;
				}
			} else {
				i++;
			}
		}
		return found;
	}

	/** BLOBMAP GIVENBLOB RETURN. */
	boolean untrackedFileGivenBranch(HashMap<String, String> blobMap,
									 HashMap<String, String> givenBlob) {
		File[] files = new File(currdir).listFiles();
		boolean found = false;
		int i = 0;
		String filename;
		if (blobMap != null) {
			while ((!found) && (i < files.length)) {
				if (!files[i].isDirectory()) {
					filename = files[i].getName();
					if (!((filename.endsWith(".zip"))
							|| (filename.endsWith(".jar"))
							|| (filename.endsWith(".DS_Store"))
							|| (filename.endsWith(".classpath"))
							|| (filename.endsWith(".project")))) {
						if (!((tracked(filename, stagingMap))
								|| (tracked(filename, blobMap)))) {
							if (givenBlob.containsKey(filename)) {
								found = true;
							} else {
								i++;
							}
						} else {
							i++;
						}
					} else {
						i++;
					}
				} else {
					i++;
				}
			}

		}
		return found;
	}
	/** BRANCHNAME. */
	void handleBranchCheckout(String branchname) {
		String branchcode = getBranchCode(branchname);
		if (branchcode == null) {
			throw new GitletException("No such branch exists");
		} else if (branchname.equals(hinfo.currbranchname)) {
			throw new GitletException("No need to checkout the current branch");
		} else if (untrackedFile()) {
			throw new GitletException("There is an untracked file in "
					+ "the way; delete it or add it first.");
		} else {
			handleCommitBranchCheckout(branchcode, branchname);
		}
	}
	void handleCommitCodeCheckout(String commitcode, String filename) {
		if (commitcode != null) {
			CommitTree cmt = locateCommit(commitcode, commitMap);
			if (cmt == null) {
				throw new GitletException("No commit with that id exists.");
			} else {
				handleFileCheckout(filename, cmt);
			}
		}
	}

	void handleCommitBranchCheckout(String branchcode, String newbranchname) {
		if (branchcode != null) {
			CommitTree cmt = locateBranch(branchcode, commitMap);
			if (cmt == null) {
				throw new GitletException("No commit with that "
						+ "branch id exists.");
			} else {
				String head = getBranchCode(hinfo.currbranchname);
				CommitTree currbranch = locateBranch(head,
											commitMap);
				HashMap<String, String> currblobMap = currbranch.com.blobs;
				HashMap<String, String> blobMap = cmt.com.blobs;
				if (currblobMap != null) {
					Iterator it = currblobMap.keySet().iterator();
					String filename;
					while (it.hasNext()) {
						filename = (String) it.next();
						if ((blobMap != null)
								&& (blobMap.get(filename) == null)) {
							Utils.restrictedDelete(filename);
						}
					}
				}
				if (blobMap != null) {
					Iterator it = blobMap.keySet().iterator();
					String filename;
					while (it.hasNext()) {
						filename = (String) it.next();
						overwriteWorkingDirectoryFile(filename, blobMap);
					}
				}
				if (stagingMap != null) {
					Iterator it = stagingMap.keySet().iterator();
					String filename;
					while (it.hasNext()) {
						filename = (String) it.next();
						Utils.restrictedDelete(filename);
					}
				}
				Utils.restrictedDelete(stagingmaploc);
				hinfo.currbranchname = newbranchname;
				hinfo.headcode = branchcode;
				Utils.writeObject(new File(headloc), hinfo);

			}
		}
	}
	void removeWorkingDirectoryFile(String filename) {
		if (new File(filename).exists()) {
			Utils.restrictedDelete(filename);
		}
	}
	void overwriteWorkingDirectoryFile(String filename,
									   HashMap<String, String> blobMap) {
		String blobname = blobMap.get(filename);
		if (blobname != null) {
			String blobfile = currdir + "/.gitlet/blobs/" + blobname;
			if (new File(blobfile).exists()) {
				byte[] content = Utils.readContents(new File(blobfile));
				Utils.writeContents(new File(filename), content);
			}
		}
	}
	/** FILENAME CMT. */
	void handleFileCheckout(String filename, CommitTree cmt) {
		HashMap<String, String> blobMap = cmt.com.blobs;
		if ((blobMap == null) || (blobMap.get(filename) == null)) {
			throw new GitletException("File does not exist in that commit.");
		} else {
			overwriteWorkingDirectoryFile(filename, blobMap);
		}
	}
	/** FILENAME. */
	public void doRm(String filename) {
		setup();
		if (untrackedFile(filename)) {
			throw new GitletException("No reason to remove the file.");
		} else {
			if ((stagingMap != null) && (stagingMap.containsKey(filename))) {
				stagingMap.remove(filename);
				Utils.writeObject(new File(stagingmaploc), stagingMap);
			}
			if (trackedInCommit(filename)) {
				if (markedSet == null) {
					markedSet = new HashSet<>();
				}
				markedSet.add(filename);
				Utils.writeObject(new File(markedloc), markedSet);
			}
			removeWorkingDirectoryFile(filename);
		}
	}
	public void doRmBranch(String branchname) {
		setup();
		if (branchMap == null) {
			throw new GitletException("A branch with that "
					+ "name does not exist.");
		} else if (!branchMap.containsKey(branchname)) {
			throw new GitletException("A branch with that "
					+ "name does not exist.");
		} else if (hinfo.currbranchname.equals(branchname)) {
			throw new GitletException("Cannot remove the current branch.");
		} else {
			branchMap.remove(branchname);
			Utils.writeObject(new File(branchloc), branchMap);
		}
	}

	ArrayList<String> getModifiedNotCommitted()
	{
		File[]files = new File(currdir).listFiles();
		boolean found = false;
		
		String filename;
		String head = getBranchCode(hinfo.currbranchname); //looking at existing branch's blob map
		//need to also check that the given branch blobMap does not overwrite the untracked file
		//a file in untracked if
		CommitTree cmt = locateBranch(head,commitMap);
		HashMap<String,String>blobMap = cmt.com.blobs;
		ArrayList<String>modified = null;
		String currentcode;
		 byte[] filecontent;
		String hashcode;
		for (int i=0;i<files.length;i++) {
				if (!files[i].isDirectory()) {
					//filename=files[i].getAbsolutePath();
					filename = files[i].getName();
					//System.out.println(filename);
					if (!((filename.endsWith(".zip"))||(filename.endsWith(".jar"))||(filename.endsWith(".DS_Store"))|| 
							(filename.endsWith(".classpath"))||(filename.endsWith(".project")))) {
						
						if (!((tracked(filename,stagingMap))||(tracked(filename,blobMap)))){
							
							found = true;
						
						}
						else {
							//this is a case of a file that is tracked but may be subsequently modified not still be committed
							currentcode = blobMap.get(filename);
								filecontent = Utils.readContents(new File (filename));
								hashcode = generateShCode(filecontent); 
								if (!hashcode.equals(currentcode)){
									if (modified == null) modified = new ArrayList<String>();
									modified.add(filename);
								}
						}
					}
				}
			}
		return modified;
	}

//	public void doStatus() {
//		setup();
//		printBranchMap(branchMap);
//		printStagingMap(stagingMap);
//		printMarkedSet(markedSet);
//		System.out.println("=== Modifications Not Staged For Commit ===");
//		System.out.println();
//		System.out.println("=== Untracked Files ===");
//		System.out.println();
//	}


	public void doStatus()
	{
		setup();
		printBranchMap(branchMap);
		printStagingMap(stagingMap);
		printMarkedSet(markedSet);
		System.out.println("=== Modifications Not Staged For Commit ===");
		ArrayList<String> modified = getModifiedNotCommitted();
		if (modified != null) {
			for (String filename:modified) {
				System.out.println(filename);
			}
		}
		System.out.println();
		System.out.println("=== Untracked Files ===");
		ArrayList<String>untrackedfiles=getUntrackedFiles();
		if (untrackedfiles != null) {
			for (String filename:untrackedfiles) {
				System.out.println(filename);
			}
		}
		System.out.println();
	}
	
	ArrayList<String> getUntrackedFiles()
	{
		File[]files = new File(currdir).listFiles();
		boolean found = false;
		
		String filename;
		String head = getBranchCode(hinfo.currbranchname); //looking at existing branch's blob map
		//need to also check that the given branch blobMap does not overwrite the untracked file
		//a file in untracked if
		CommitTree cmt =  locateBranch(head,commitMap);
		HashMap<String,String>blobMap = cmt.com.blobs;
		ArrayList<String>untracked = null;
		for (int i=0;i<files.length;i++) {
				if (!files[i].isDirectory()) {
					//filename=files[i].getAbsolutePath();
					filename = files[i].getName();
					//System.out.println(filename);
					if (!((filename.endsWith(".zip"))||(filename.endsWith(".jar"))||(filename.endsWith(".DS_Store"))|| 
							(filename.endsWith(".classpath"))||(filename.endsWith(".project")))) {
						
						if (!((tracked(filename,stagingMap))||(tracked(filename,blobMap)))){
							
							found = true;
							if (untracked==null) untracked = new ArrayList<String>();
							untracked.add(filename);
						}
					}
				}
			}
		return untracked;
	}
	public void doFind(String commitmessage) {
		setup();
		if (commitMap != null) {
			boolean found = findCommitInTree(commitmessage, commitMap);
			if (!found) {
				throw new GitletException("Found no commit with that message.");
			}
		} else {
			throw new GitletException("Found no commit with that message.");
		}
	}
	/** RETURN. */
	public void doLog() {
		setup();
		if (commitMap == null) {
			throw new GitletException("Nothing committed yet");
		}
		CommitTree cmt = locateBranch(
				getBranchCode(hinfo.currbranchname), commitMap);
		printLog(cmt);

	}
	/** RETURN. */
	public void doGlobalLog() {
		setup();
		if (commitMap == null) {
			throw new GitletException("Nothing committed yet");
		} else if (branchMap != null) {
			Iterator it = branchMap.keySet().iterator();
			String branchname;
			CommitTree branch;
			while (it.hasNext()) {
				branchname = (String) it.next();
				branch = locateBranch(getBranchCode(branchname), commitMap);
				printLog(branch);
			}
		}
	}
	/** RETURN. */
	@SuppressWarnings("unchecked")
	void setup() {
		headloc = currdir + "/.gitlet/head.txt";
		commitloc = currdir + "/.gitlet/commitmap";
		stagingmaploc = currdir + "/stagingmap";
		blobloc = currdir + "/.gitlet/blobs";
		branchloc = currdir + "/.gitlet/branch";
		markedloc = currdir + "/.gitlet/marked";
		if ((new File(headloc)).exists()) {
			hinfo = Utils.readObject(new File(headloc), HeadInfo.class);
		} else {
			hinfo = null;
		}
		if ((new File(commitloc)).exists()) {
			commitMap = Utils.readObject(new File(commitloc), CommitTree.class);
		} else {
			commitMap = null;
		}
		if ((new File(stagingmaploc)).exists()) {
			stagingMap = Utils.readObject(new File(stagingmaploc),
							HashMap.class);
		} else {
			stagingMap = null;
		}
		if ((new File(branchloc)).exists()) {
			branchMap = Utils.readObject(new File(branchloc), HashMap.class);
		} else {
			branchMap = null;
		}
		if ((new File(markedloc)).exists()) {
			markedSet = Utils.readObject(new File(markedloc), HashSet.class);
		} else {
			markedSet = null;
		}
	}
	/** HASHCODE FILENAME. */
	void createBlob(String hashcode, String filename) {
		File blobdir = new File(blobloc);
		if (!blobdir.exists()) {
			blobdir.mkdir();
		}
		File blobfile = new File(blobdir.getAbsolutePath() + "/" + hashcode);
		Utils.writeContents(blobfile, Utils.readContents(new File(filename)));

	}
	/** MESSAGE TREE RETURN. */
	boolean findCommitInTree(String message,CommitTree tree)
	{
			boolean find = false;
			if (tree != null)
			{
				if (tree.com.message.equals(message)) {
					System.out.println(tree.code);
					find = true;
				}
				if (tree.children != null)
					for (CommitTree child:tree.children) {
						find = findCommitInTree(message,child)||find;
					}
			}
			return find;
	}
	/** BRANCHCODE TREE RETURN. */
	CommitTree locateBranch(String branchcode, CommitTree tree) {
		return locateNode(branchcode, tree);
	}
	/** COMMITCODE TREE RETURN. */
	CommitTree locateCommit(String commitcode, CommitTree tree) {
		return locateNode(commitcode, tree);
	}
	/** BRANCHCODE TREE RETURN. */
	CommitTree locateNode(String branchcode,CommitTree tree)
	{
		if (tree != null)
		{
			if (tree.code.equals(branchcode)) {
				return tree;
			}
			else if (tree.children != null) {
				boolean found = false;
				int i=0;
				CommitTree match = null;
				
				while ((!found) && (i<tree.children.size())) {
					match = locateNode(branchcode,tree.children.get(i));
					if (match != null) {
						found = true;
					}
					else i++;
				}
				return match;
			}	
			else {
				return null;
			}
		
		}
		else {
			return null;
		}
	}
	/** TREE. */
	void printLog(CommitTree tree)
	{
		if (tree != null)
		{
			System.out.println("===");
			System.out.println("commit "+tree.code);
			if ((tree.parents != null)&& (tree.parents.size()>1)) {
				String mergestring = "Merge:";
				for (int i=0;i<tree.parents.size();i++) {
					mergestring+= " "+(tree.parents.get(i).code).substring(0,7);
				}
			}
			System.out.println("Date: "+getDateString(tree.com.time));
			System.out.println (tree.com.message);
			System.out.println();
			if (tree.parents != null) {
				printLog(tree.parents.get(0));
			}

		}
	}
	String getDateString(Date date) {
        if (date == null) {
            return null;
        }
        SimpleDateFormat sdf = new SimpleDateFormat("E MMM dd HH:mm:ss yyyy Z");
        sdf.setTimeZone(DEFAULT_TIMEZONE);
        return sdf.format(date);
    }

	HashMap<String, String> addToStaging(String filename, String hashcode) {
		if (stagingMap == null) {
			stagingMap = new HashMap<>();
		}
		stagingMap.put(filename, hashcode);
		return stagingMap;
	}

	public void doAdd(String filename) {
		if (!(new File(filename)).exists()) {
			throw new GitletException("File does not exist.");
		}
		setup();
		byte[] filecontent = Utils.readContents(new File(filename));
		String hashcode = generateShCode(filecontent);
		String branchcode = getBranchCode(hinfo.currbranchname);
	    CommitTree exist = locateBranch(branchcode, commitMap);
	    if (exist != null)  {
	    	Commit cmt = exist.com;
	    	HashMap<String, String> blobMap = cmt.blobs;
	    	if (blobMap == null) {
	    		createBlob(hashcode, filename);
	    		addToStaging(filename, hashcode);
	    		Utils.writeObject(new File(stagingmaploc), stagingMap);
	    	} else {
	    		String blobcode = blobMap.get(filename);
	    		if (blobcode == null) {
	    			createBlob(hashcode, filename);
	    			addToStaging(filename, hashcode);
		    		Utils.writeObject(new File(stagingmaploc), stagingMap);
	    		} else if (blobcode.equals(hashcode)) {
	    			if ((stagingMap != null)
							&& (stagingMap.containsKey(filename))) {
	    				stagingMap.remove(filename);
	    			}
	    			if ((markedSet != null) && (markedSet.contains(filename))) {
	    				markedSet.remove(filename);
	    			}
	    			Utils.writeObject(new File(stagingmaploc), stagingMap);
	    			Utils.writeObject(new File(markedloc), markedSet);
	    		} else {
	    			createBlob(hashcode, filename);
		    		addToStaging(filename, hashcode);
		    		Utils.writeObject(new File(stagingmaploc), stagingMap);
	    		}
	    	}
	    	if ((markedSet != null) && (markedSet.contains(filename))) {
	    		markedSet.remove(filename);
	    		Utils.writeObject(new File(markedloc), markedSet);
	    	}
	    } else {
	    	throw new GitletException("Improperly initialized");
	    }
	}
	/** COMBYTES RETURN. */
	String generateShCode(byte[] combytes) {
		String hashcode = Utils.sha1(combytes);
		return hashcode;
	}
	/** RETURN. */
	public void doInit() {
		setup();
		File outfile = new File(commitloc);
		File headfile = new File(headloc);
		File branchfile = new File(branchloc);
		if (!outfile.exists()) {
			Commit com = new Commit();
			String hashcode = generateShCode(Utils.serialize(com));
			commitMap = new CommitTree();
			commitMap.com = com;
			commitMap.code = hashcode;
			if (hinfo == null) {
				hinfo = new HeadInfo();
			}
			hinfo.headcode = hashcode;
			hinfo.currbranchname = "master";

			Utils.writeObject(outfile, commitMap);
			Utils.writeObject(new File(headloc), hinfo);
			if (branchMap == null) {
				branchMap = new HashMap<>();
			}
			branchMap.put("master", hinfo.headcode);
			Utils.writeObject(branchfile, branchMap);
		} else {
			throw new GitletException("A Gitlet version-control system "
					+ "already exists in the current directory");
		}
	}
}