package gitlet;

import java.io.Serializable;
import java.util.ArrayList;

public class CommitTree implements Serializable {

	Commit com;
	String code;
	//CommitTree parent;
	//CommitTree mergeparent;
	ArrayList<CommitTree>parents;
	ArrayList<CommitTree>children;

}
