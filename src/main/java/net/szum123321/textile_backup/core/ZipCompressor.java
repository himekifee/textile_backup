/*
    A simple backup mod for Fabric
    Copyright (C) 2020  Szum123321

    This program is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with this program.  If not, see <https://www.gnu.org/licenses/>.
*/

package net.szum123321.textile_backup.core;

import net.minecraft.server.command.ServerCommandSource;
import net.szum123321.textile_backup.TextileBackup;
import org.apache.commons.compress.archivers.tar.TarArchiveEntry;
import org.apache.commons.compress.archivers.tar.TarArchiveOutputStream;
import org.apache.commons.compress.compressors.bzip2.BZip2CompressorOutputStream;
import org.apache.commons.compress.utils.IOUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.time.LocalDateTime;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

public class ZipCompressor {
    public static void createArchive(File in, File out, ServerCommandSource ctx){
        Utilities.log("Starting compression...", ctx);

        try (ZipOutputStream arc = new ZipOutputStream(new FileOutputStream(out))){
            arc.setLevel(TextileBackup.config.compression);
            arc.setComment("Created on: " + Utilities.getDateTimeFormatter().format(LocalDateTime.now()));

            File input = in.getCanonicalFile();
            int rootPathLength = input.toString().length() + 1;

            Files.walk(input.toPath()).filter(path -> !path.equals(input.toPath()) && path.toFile().isFile() && !TextileBackup.config.fileBlacklist.contains(path.toString().substring(rootPathLength))).forEach(path -> {
                try{
                    File file = path.toAbsolutePath().toFile();

                    ZipEntry entry = new ZipEntry(file.getAbsolutePath().substring(rootPathLength));
                    arc.putNextEntry(entry);
                    entry.setSize(file.length());
                    IOUtils.copy(new FileInputStream(file), arc);
                    arc.closeEntry();
                }catch (IOException e){
                    TextileBackup.logger.error(e.getMessage());
                }
            });

        } catch (IOException e) {
            TextileBackup.logger.error(e.getMessage());
        }

        Utilities.log("Compression finished", ctx);
    }
}
