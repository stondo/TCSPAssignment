package com.swisscom.tcsp;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.swisscom.tcsp.model.TreeItem;
import com.swisscom.tcsp.service.TreeComparator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.Optional;

public class Main {

    private static final Logger logger = LoggerFactory.getLogger(Main.class);

    public static void main(String[] args) {
        ObjectMapper mapper = new ObjectMapper();
        try {
            File initialFile = new File("src/main/resources/initial_tree.json");
            File newFile = new File("src/main/resources/new_tree.json");

            logger.info("Reading initial JSON from {}", initialFile.getAbsolutePath());
            TreeItem initialTree = mapper.readValue(initialFile, TreeItem.class);

            logger.info("Reading new JSON from {}", newFile.getAbsolutePath());
            TreeItem newTree = mapper.readValue(newFile, TreeItem.class);

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
