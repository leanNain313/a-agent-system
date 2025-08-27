package com.ai.job;

import com.ai.contant.AppConstant;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.io.File;

import static com.ai.contant.AppConstant.IMAGE_TEMP_DIR;
import static com.ai.contant.AppConstant.SCREEN_IMAGE_DIR;

@Component
public class FileCleanupTask {


    // 每天凌晨2点执行一次，Cron表达式可以根据需要修改
    @Scheduled(cron = "0 0 2 * * ?")
    public void deleteFile() {
        deleteFolderContents(SCREEN_IMAGE_DIR);
        deleteFolderContents(IMAGE_TEMP_DIR);
    }

    private void deleteFolderContents(String path) {
        File folder = new File(path);
        if (folder.exists() && folder.isDirectory()) {
            boolean result = deleteFolder(folder);
            if (result) {
                System.out.println("文件夹清理完成: " + path);
            } else {
                System.out.println("文件夹清理失败: " + path);
            }
        } else {
            System.out.println("文件夹不存在: " + path);
        }
    }

    // 递归删除文件夹及其内容
    private boolean deleteFolder(File folder) {
        File[] files = folder.listFiles();
        if (files != null) {
            for (File file : files) {
                if (file.isDirectory()) {
                    deleteFolder(file);
                } else {
                    file.delete();
                }
            }
        }
        // 删除空文件夹（可选，如果不想删除根文件夹可以注释掉）
        return folder.delete();
    }
}
