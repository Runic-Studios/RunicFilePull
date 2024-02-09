package com.runicrealms.plugin.filepull.operation;

import com.runicrealms.plugin.filepull.FileUtils;
import com.runicrealms.plugin.filepull.RunicFilePull;
import com.runicrealms.plugin.filepull.target.Target;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.Bukkit;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.lib.PersonIdent;
import org.eclipse.jgit.transport.CredentialsProvider;
import org.eclipse.jgit.transport.UsernamePasswordCredentialsProvider;

import java.io.File;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class FilePushOperation {

    public FilePushOperation(Runnable onComplete) {
        Bukkit.getScheduler().runTaskAsynchronously(RunicFilePull.getInstance(), () -> {
            try {
                Bukkit.broadcastMessage(ChatColor.DARK_GREEN + "FilePUSH Initiated! I hope you know what you are doing...");

                Bukkit.broadcastMessage(ChatColor.GREEN + "Cloning git repository locally...");

                File cloneDir = new File(RunicFilePull.getInstance().getDataFolder(), "git-clone");
                if (cloneDir.exists()) {
                    FileUtils.deleteDirectory(cloneDir);
                }
                CredentialsProvider credentials = new UsernamePasswordCredentialsProvider(RunicFilePull.GH_AUTH_TOKEN, "");
                Git git = Git.cloneRepository()
                        .setURI("https://github.com/" + RunicFilePull.getInstance().getFileConfig().getTargetRepo() + ".git")
                        .setBranch(RunicFilePull.getInstance().getFileConfig().getTargetBranch())
                        .setDirectory(cloneDir)
                        .setCredentialsProvider(credentials)
                        .call();

                Bukkit.broadcastMessage(ChatColor.GREEN + "Deleting and copying files...");

                try {
                    for (Target target : RunicFilePull.getInstance().getFileConfig().getTargets()) {
                        target.copyLocal(cloneDir);
                    }
                } catch (Exception exception) {
                    Bukkit.broadcastMessage(ChatColor.RED + "Error copying: " + exception.getMessage());
                    Bukkit.broadcastMessage(ChatColor.RED + "Aborting... ");
                    exception.printStackTrace();
                    return;
                }

                Bukkit.broadcastMessage(ChatColor.GREEN + "Pushing to git...");

                git.add().addFilepattern(".").call();
                git.commit()
                        .setMessage("FilePush: " + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")))
                        .setAuthor(new PersonIdent("RunicRealmsGithub", "runicrealms.mc@gmail.com"))
                        .call();
                git.push().setCredentialsProvider(credentials).call();
                git.close();

                Bukkit.broadcastMessage(ChatColor.GREEN + "Deleting git cache...");

                if (cloneDir.exists()) {
                    FileUtils.deleteDirectory(cloneDir);
                }

                Bukkit.broadcastMessage(ChatColor.GREEN + "Filepush done!");
            } catch (IOException | GitAPIException exception) {
                Bukkit.broadcastMessage(ChatColor.RED + "There was an error with filepush: " + exception.getMessage());
                exception.printStackTrace();
            } finally {
                onComplete.run();
            }

        });
    }

}
