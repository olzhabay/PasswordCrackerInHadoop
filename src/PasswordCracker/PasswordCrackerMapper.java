package PasswordCracker;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Mapper;

import java.io.IOException;

public class PasswordCrackerMapper
        extends Mapper<Text, Text, Text, Text> {

    //  After reading a key/value, it compute the password by using a function of PasswordCrackerUtil class
    //  If it receive the original password, pass the original password to reducer. Otherwise is not.
    //  FileSystem class : refer to https://hadoop.apache.org/docs/r2.7.3/api/org/apache/hadoop/fs/FileSystem.html

    @Override
    public void map(Text key, Text value, Context context)
            throws IOException, InterruptedException {
        System.out.println("DEBUG mapper: start");
        Configuration conf = context.getConfiguration();
        String flagFilename = conf.get("terminationFlagFilename");
        FileSystem hdfs = FileSystem.get(conf);

        TerminationChecker terminationChecker = new TerminationChecker(hdfs, flagFilename);

        long rangeBegin = Long.parseLong(key.toString());
        long rangeEnd = Long.parseLong(value.toString());
        System.out.println("DEBUG mapper: range " + rangeBegin + " " + rangeEnd);
        String encryptedPassword = conf.get("encryptedPassword");
        String password = PasswordCrackerUtil.findPasswordInRange(rangeBegin, rangeEnd, encryptedPassword, terminationChecker);
        System.out.println("DEBUG mapper: password " + password);
        if (password != null) {
            context.write(new Text(encryptedPassword), new Text(password));
        }
    }
}

//  It is class for early termination.
//  In this assignment, a particular file becomes an early termination signal.
//  So, If a task find the original password, then the task creates a file using a function in this class.
//  Therefore, tasks will determine whether the quit or not by checking presence of file.
//  FileSystem class : refer to https://hadoop.apache.org/docs/r2.7.3/api/org/apache/hadoop/fs/FileSystem.html

class TerminationChecker {
    private FileSystem fs;
    private Path flagPath;
    private boolean isTerminated;

    TerminationChecker(FileSystem fs, String flagFilename) {
        this.fs = fs;
        this.flagPath = new Path(flagFilename);
        try {
            fs.delete(flagPath, true);
        } catch (IOException e) {
            e.printStackTrace();
        }
        isTerminated = false;
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    asyncTerminationChecker();
                } catch (IOException | InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }

    public boolean isTerminated() throws IOException {
	    return isTerminated;
    }

    public void setTerminated() throws IOException {
	    isTerminated = true;
	    fs.create(flagPath);
    }

    private void asyncTerminationChecker() throws IOException, InterruptedException {
        while (!isTerminated) {
            Thread.sleep(5000);
            isTerminated = fs.exists(flagPath);
        }
    }
}
