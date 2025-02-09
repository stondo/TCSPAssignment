package com.swisscom.tcsp.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.swisscom.tcsp.model.TreeItem;
import org.junit.jupiter.api.Test;

import java.io.InputStream;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class TreeComparatorTest {

    private final ObjectMapper mapper = new ObjectMapper();

    @Test
    public void testCompareTrees() throws Exception {

        InputStream initialIS = getClass().getResourceAsStream("/initial_tree.json");
        assert initialIS != null : "initial_tree.json not found in resources";
        TreeItem initialTree = mapper.readValue(initialIS, TreeItem.class);

        InputStream newIS = getClass().getResourceAsStream("/new_tree.json");
        assert newIS != null : "new_tree.json not found in resources";
        TreeItem newTree = mapper.readValue(newIS, TreeItem.class);

        Optional<TreeItem> resultOpt = TreeComparator.compareNodes(initialTree, newTree);
        assertTrue(resultOpt.isPresent(), "Comparison result should be present");
        TreeItem result = resultOpt.get();

        InputStream expectedIS = getClass().getResourceAsStream("/order_tree.json");
        assert expectedIS != null : "order_tree.json not found in resources";
        TreeItem expected = mapper.readValue(expectedIS, TreeItem.class);

        String expectedJson = mapper.writerWithDefaultPrettyPrinter().writeValueAsString(expected);
        String resultJson = mapper.writerWithDefaultPrettyPrinter().writeValueAsString(result);

        System.out.println("Expected JSON:\n" + expectedJson);
        System.out.println("Result JSON:\n" + resultJson);

        assertEquals(expectedJson, resultJson, "The merged tree should match the expected output");
    }
}
