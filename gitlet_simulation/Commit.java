package gitlet;
import java.io.Serializable;
import java.sql.Timestamp;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Date;
import java.util.HashMap;

public class Commit implements Serializable {

    Date time;
    String message;
    HashMap<String, String> blobs;
    String branch;

    public Commit()  {
        this.message = "initial commit";
        Timestamp ts = new Timestamp(
                ZonedDateTime.of(1970, 1, 1, 0, 0, 0, 0,
                        ZoneId.of("UTC")
                ).toInstant().toEpochMilli());
        this.time = ts;
        this.branch = "master";
    }
}
