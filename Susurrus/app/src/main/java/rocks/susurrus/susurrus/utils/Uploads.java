package rocks.susurrus.susurrus.utils;

import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.provider.OpenableColumns;
import android.util.Log;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;

/**
 * Created by simon on 08.07.15.
 */
public class Uploads {
    static final String LOG_TAG = "Uploads";

    /**
     * Fetch an inputStream from an URI and parse it into an bufferd byte array for marshalling.
     *
     * @param resolver
     * @param path
     * @return
     *
     * Further reading: https://stackoverflow.com/questions/10296734/image-uri-to-bytesarray
     */
    public static ArrayList uriToStream(ContentResolver resolver, Uri path) {
        ArrayList resultArray = new ArrayList();

        // get input stream of the uri
        InputStream inputStream;
        try {
            inputStream = resolver.openInputStream(path);
        } catch(FileNotFoundException e) {
            e.printStackTrace();

            return null;
        }

        ByteArrayOutputStream byteBuffer = new ByteArrayOutputStream();

        // this is storage overwritten on each iteration with bytes
        int bufferSize = 1024;
        byte[] buffer = new byte[bufferSize];

        // we need to know how may bytes were read to write them to the byteBuffer
        try {
            int len = 0;
            while ((len = inputStream.read(buffer)) != -1) {
                byteBuffer.write(buffer, 0, len);
            }
        } catch(IOException e) {
            e.printStackTrace();

            return null;
        }

        // and then we can return your byte array.
        byte[] stream = byteBuffer.toByteArray();
        int streamLength = byteBuffer.size();
        resultArray.add(stream);
        resultArray.add(streamLength);

        return resultArray;
    }

    /**
     * Extracts a file-extension from a given URI.
     *
     * @param _fileName File-Name.
     * @return Extension-String or null.
     */
    public static String getFileExtension(String _fileName) {

        int extensionPosition = _fileName.lastIndexOf(".") + 1;
        String extensionName = _fileName.substring(extensionPosition);

        return extensionName;
    }

    /**
     * Extracts a file-name from a given URI.
     *
     * @param _path File-URI.
     * @return File-String or null.
     */
    public static String getFileName(ContentResolver _resolver, Uri _path) {

        Cursor cursor = _resolver.query(_path, null, null, null, null);
        String fileName = null;
        try {

            if(cursor != null && cursor.moveToFirst()) {
                fileName = cursor.getString(cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME));
            }

        } finally {
            cursor.close();
        }

        return fileName;
    }
}
