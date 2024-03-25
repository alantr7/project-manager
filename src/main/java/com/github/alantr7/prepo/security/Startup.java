package com.github.alantr7.prepo.security;

import io.quarkus.runtime.StartupEvent;

import java.io.File;
import java.nio.file.Files;

import javax.enterprise.event.Observes;
import javax.inject.Singleton;
import javax.transaction.Transactional;

@Singleton
public class Startup {

    @Transactional
    public void prepareFilesAndFolders(@Observes StartupEvent evt) {

        // Check if default avatar exists
        var avatarsFolder = new File(System.getProperty("user.dir"), "avatars");
        avatarsFolder.mkdirs();

        var defaultAvatarFile = new File(avatarsFolder, "default.png");
        if (!defaultAvatarFile.exists()) {
            try {
                var avatarUrl = Startup.class.getClassLoader().getResourceAsStream("default-avatar.png");
                Files.copy(avatarUrl, defaultAvatarFile.toPath());
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

    }

}
