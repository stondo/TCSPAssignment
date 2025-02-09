package com.swisscom.tcsp;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.swisscom.tcsp.model.TreeItem;
import com.swisscom.tcsp.service.TreeComparator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.InputStream;
import java.util.Optional;

public class Main {

    private static final Logger logger = LoggerFactory.getLogger(Main.class);

    public static void main(String[] args) {
        ObjectMapper mapper = new ObjectMapper();
        try {
            InputStream initialIS = Main.class.getResourceAsStream("/initial_tree.json");
            if (initialIS == null) {
                logger.error("initial_tree.json not found on the classpath.");
                return;
            }
            logger.info("Reading initial JSON from classpath resource: /initial_tree.json");
            TreeItem initialTree = mapper.readValue(initialIS, TreeItem.class);

            InputStream newIS = Main.class.getResourceAsStream("/new_tree.json");
            if (newIS == null) {
                logger.error("new_tree.json not found on the classpath.");
                return;
            }
            logger.info("Reading new JSON from classpath resource: /new_tree.json");
            TreeItem newTree = mapper.readValue(newIS, TreeItem.class);

            Optional<TreeItem> comparedTreeOpt = TreeComparator.compareNodes(initialTree, newTree);

            if (comparedTreeOpt.isPresent()) {
                TreeItem comparedTree = comparedTreeOpt.get();

                File outputFile = new File("order.json");
                mapper.writerWithDefaultPrettyPrinter().writeValue(outputFile, comparedTree);
                logger.info("Comparison complete. Output written to {}", outputFile.getAbsolutePath());
            } else {
                logger.error("Comparison returned an empty result. Check your input JSONs.");
            }
        } catch (Exception e) {
            logger.error("Error occurred during tree comparison", e);
        }
    }
}
