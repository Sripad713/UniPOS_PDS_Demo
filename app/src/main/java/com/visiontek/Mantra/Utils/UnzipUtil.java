package com.visiontek.Mantra.Utils;

import android.util.Log;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

public class UnzipUtil
{
    //-Dfile.encoding=UTF-8
    private final String zipFile;
    private final String location;

    public UnzipUtil(String zipFile, String location)
    {
        this.zipFile = zipFile;
        this.location = location;

        dirChecker("");
    }

    public void unzip()
    {
        try
        {
            FileInputStream fin = new FileInputStream(zipFile);
            ZipInputStream zin = new ZipInputStream(fin);
            ZipEntry ze = null;
            while ((ze = zin.getNextEntry()) != null)
            {
                Log.v("Decompress", "Unzipping " + ze.getName());

                if(ze.isDirectory())
                {
                    dirChecker(ze.getName());
                }
                else
                {
                    FileOutputStream fout = new FileOutputStream(location + ze.getName());

                    byte[] buffer = new byte[8192];
                    int len;
                    while ((len = zin.read(buffer)) != -1)
                    {
                        fout.write(buffer, 0, len);
                    }
                    fout.close();

                    zin.closeEntry();

                }

            }
            zin.close();
        }
        catch(Exception e)
        {
            Log.e("Decompress", "unzip", e);
        }

    }

    private void dirChecker(String dir)
    {
        File f = new File(location + dir);
        if(!f.isDirectory())
        {
            f.mkdirs();
        }
    }

    public static void zip(String[] _files, String zipFileName) {
        try {
            BufferedInputStream origin = null;
            FileOutputStream dest = new FileOutputStream(zipFileName);
            ZipOutputStream out = new ZipOutputStream(new BufferedOutputStream(
                    dest));
            final int BUFFER = 1024;
            byte data[] = new byte[BUFFER];

            for (int i = 0; i < _files.length; i++) {
                Log.v("Compress", "Adding: " + _files[i]);
                File file = new File(_files[i]);
                if(file.exists() && file.isFile() && file.length() > 0);
                else
                    continue;
                FileInputStream fi = new FileInputStream(_files[i]);
                origin = new BufferedInputStream(fi, BUFFER);

                ZipEntry entry = new ZipEntry(_files[i].substring(_files[i].lastIndexOf("/") + 1));
                out.putNextEntry(entry);
                int count;

                while ((count = origin.read(data, 0, BUFFER)) != -1) {
                    out.write(data, 0, count);
                }
                origin.close();
            }

            out.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}