package info.ata4.test;

import info.ata4.junity.bundle.Bundle;
import info.ata4.junity.bundle.BundleReader;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;

/**
 * desc:
 *
 */
public class TestBundleInfo {
    public static final String ASSET_ROOT = "/Users/yanmaoyuan/Downloads/女神星球/xzn2_gwhznew/";

    public static void main(String[] args) {
        File file = new File(ASSET_ROOT + "assets/unit_prefab/character_prefab/res_chr002");
        Path path = file.toPath();

        try (BundleReader reader = new BundleReader(path)) {
            Bundle bundle = reader.read();
            System.out.println(bundle);
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }
}
