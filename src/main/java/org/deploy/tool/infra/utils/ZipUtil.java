package org.deploy.tool.infra.utils;


import org.apache.commons.compress.archivers.zip.ZipArchiveEntry;
import org.apache.commons.compress.archivers.zip.ZipArchiveOutputStream;
import org.apache.commons.compress.archivers.zip.ZipFile;
import org.apache.commons.io.IOUtils;

import java.io.*;
import java.util.Enumeration;
import java.util.zip.Deflater;

public class ZipUtil {

  /**
   * 压缩文件或目录
   *
   * @param sourcePath  要压缩的文件或目录路径
   * @param zipFilePath 生成的ZIP文件路径
   * @throws IOException 如果发生I/O错误
   */
  public static void compress(String sourcePath, String zipFilePath) {
    File sourceFile = new File(sourcePath);
    if (!sourceFile.exists()) {
      throw new RuntimeException("源文件或目录不存在: " + sourcePath);
    }
    try (FileOutputStream fos = new FileOutputStream(zipFilePath);
        ZipArchiveOutputStream zos = new ZipArchiveOutputStream(fos)) {
      // 设置ZIP文件使用UTF-8编码，支持中文文件名
      zos.setEncoding("UTF-8");
      // 设置默认压缩级别（6 - 平衡速度和压缩率）
      zos.setLevel(Deflater.DEFAULT_COMPRESSION);
      // 递归添加文件到ZIP
      addToZip(sourceFile, sourceFile.getName(), zos);
      // 完成压缩过程
      zos.finish();
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  /**
   * 递归添加文件到ZIP输出流
   *
   * @param file     当前处理的文件或目录
   * @param basePath ZIP中的基础路径
   * @param zos      ZIP输出流
   * @throws IOException 如果发生I/O错误
   */
  private static void addToZip(File file, String basePath, ZipArchiveOutputStream zos)
      throws IOException {
    // 处理目录
    if (file.isDirectory()) {
      // 确保目录路径以"/"结尾
      String dirPath = basePath + (basePath.endsWith("/") ? "" : "/");

      // 创建目录条目（即使为空目录也创建）
      ZipArchiveEntry dirEntry = new ZipArchiveEntry(dirPath);
      zos.putArchiveEntry(dirEntry);
      zos.closeArchiveEntry();

      // 递归处理目录内容
      File[] children = file.listFiles();
      if (children != null) {
        for (File child : children) {
          addToZip(child, dirPath + child.getName(), zos);
        }
      }
    }
    // 处理文件
    else {
      try (FileInputStream fis = new FileInputStream(file)) {
        // 创建文件条目
        ZipArchiveEntry entry = new ZipArchiveEntry(file, basePath);
        zos.putArchiveEntry(entry);
        // 写入文件内容
        IOUtils.copy(fis, zos);
        // 关闭当前条目
        zos.closeArchiveEntry();
      }
    }
  }

  /**
   * 解压ZIP文件
   *
   * @param zipFilePath ZIP文件路径
   * @param outputDir   解压输出目录
   * @throws IOException 如果发生I/O错误
   */
  public static void extract(File zipFile, String outputDir) throws IOException {
    if (!zipFile.exists()) {
      throw new FileNotFoundException("ZIP文件不存在: " + zipFile);
    }

    File outputDirFile = new File(outputDir);
    if (!outputDirFile.exists() && !outputDirFile.mkdirs()) {
      throw new IOException("无法创建输出目录: " + outputDir);
    }
    try (ZipFile zf = new ZipFile(zipFile, "UTF-8")) {
      Enumeration<ZipArchiveEntry> entries = zf.getEntries();
      while (entries.hasMoreElements()) {
        ZipArchiveEntry entry = entries.nextElement();
        File outputFile = new File(outputDir, entry.getName());

        // 处理目录条目
        if (entry.isDirectory()) {
          if (!outputFile.exists() && !outputFile.mkdirs()) {
            throw new IOException("无法创建目录: " + outputFile);
          }
        }
        // 处理文件条目
        else {
          // 确保父目录存在
          File parentDir = outputFile.getParentFile();
          if (!parentDir.exists() && !parentDir.mkdirs()) {
            throw new IOException("无法创建目录: " + parentDir);
          }
          try (InputStream is = zf.getInputStream(entry);
              FileOutputStream fos = new FileOutputStream(outputFile)) {
            // 复制文件内容
            IOUtils.copy(is, fos);
          }
        }
      }
    }
  }
}
