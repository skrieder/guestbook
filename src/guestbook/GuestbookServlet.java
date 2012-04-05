package guestbook;

import com.google.appengine.api.files.AppEngineFile;
import com.google.appengine.api.files.FileReadChannel;
import com.google.appengine.api.files.FileService;
import com.google.appengine.api.files.FileServiceFactory;
import com.google.appengine.api.files.FileWriteChannel;
import com.google.appengine.api.files.GSFileOptions.GSFileOptionsBuilder;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.ByteBuffer;
import java.nio.channels.Channels;

import javax.servlet.http.*;

/**
 * Copyright 2011 Google Inc. All Rights Reserved.
 * Create, Write, Read, and Finalize Cloud Storage objects.
 * Access your app at: http://myapp.appspot.com/
**/

@SuppressWarnings("serial")
public class GuestbookServlet extends HttpServlet {
   public static final String BUCKETNAME = "skrieder";
   public static final String FILENAME = "test.txt";

  @Override
  public void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
    resp.setContentType("text/plain");
    resp.getWriter().println("Hello, world from java");
    FileService fileService = FileServiceFactory.getFileService();
    GSFileOptionsBuilder optionsBuilder = new GSFileOptionsBuilder()
       .setBucket(BUCKETNAME)
       .setKey("test3.txt")
       .setMimeType("text/html")
       .setAcl("public_read")
       .addUserMetadata("myfield1", "my field value");
    AppEngineFile writableFile =
         fileService.createNewGSFile(optionsBuilder.build());
    // Open a channel to write to it
     boolean lock = false;
     FileWriteChannel writeChannel =
         fileService.openWriteChannel(writableFile, lock);
     // Different standard Java ways of writing to the channel
     // are possible. Here we use a PrintWriter:
     PrintWriter out = new PrintWriter(Channels.newWriter(writeChannel, "UTF8"));
     //open local file
     // read local file
     //output local file to gae file
     out.println("The woods are lovely dark and deep.");
     out.println("But I have promises to keep.");
     // Close without finalizing and save the file path for writing later
     out.close();
     String path = writableFile.getFullPath();
     // Write more to the file in a separate request:
     writableFile = new AppEngineFile(path);
     // Lock the file because we intend to finalize it and
     // no one else should be able to edit it
     lock = true;
     writeChannel = fileService.openWriteChannel(writableFile, lock);
     // This time we write to the channel directly
     writeChannel.write(ByteBuffer.wrap
               ("And miles to go before I sleep.".getBytes()));

     // Now finalize
     writeChannel.closeFinally();
     resp.getWriter().println("Done writing...");

     // At this point, the file is visible in App Engine as:
     // "/gs/BUCKETNAME/FILENAME"
     // and to anybody on the Internet through Cloud Storage as:
     // (http://commondatastorage.googleapis.com/BUCKETNAME/FILENAME)
     // We can now read the file through the API:
     String filename = "/gs/" + BUCKETNAME + "/" + FILENAME;
     AppEngineFile readableFile = new AppEngineFile(filename);
     FileReadChannel readChannel =
         fileService.openReadChannel(readableFile, false);
     // Again, different standard Java ways of reading from the channel.
     BufferedReader reader =
             new BufferedReader(Channels.newReader(readChannel, "UTF8"));
     String line = reader.readLine();
     resp.getWriter().println("READ:" + line);

    // line = "The woods are lovely, dark, and deep."
     readChannel.close();
  }
}