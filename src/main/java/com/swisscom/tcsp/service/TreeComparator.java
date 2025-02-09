package com.swisscom.tcsp.service;

import com.swisscom.tcsp.model.Attribute;
import com.swisscom.tcsp.model.TreeItem;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.stream.Collectors;

public class TreeComparator {

    private static final Logger logger = LoggerFactory.getLogger(TreeComparator.class);

    /**
     * Recursively compares two TreeItem nodes.
     * <p>
     * If both nodes are null, returns Optional.empty(). If one node is null, returns that node
     * cloned with the appropriate operation (CREATE or DELETE). If both exist, compares attributes,
     * relations, and children to decide between NO_ACTION or UPDATE.
     *
     * @param initial the node from the initial tree (may be null)
     * @param newItem the node from the new tree (may be null)
     * @return an Optional containing the merged TreeItem with computed operation,
     *         or Optional.empty() if both are null
     */
    public static Optional<TreeItem> compareNodes(TreeItem initial, TreeItem newItem) {
        logger.info("Comparing nodes: initial={} vs new={}",
                (initial != null ? initial.id() : "null"),
                (newItem != null ? newItem.id() : "null"));

        if (initial == null && newItem == null) {
            logger.warn("Both nodes are null, returning Optional.empty()");
            return Optional.empty();
        }

        if (initial == null) {
            logger.info("Initial node is null; marking new node {} as CREATE", newItem.id());
            return Optional.of(cloneWithOperation(newItem, "CREATE"));
        }

        if (newItem == null) {
            logger.info("New node is null; marking initial node {} as DELETE", initial.id());
            return Optional.of(cloneWithOperation(initial, "DELETE"));
        }

        // Both nodes exist. With Jackson configured to replace null lists with empty lists,
        // we assume attributes(), children(), and relations() are non-null.
        List<Attribute> comparedAttributes = compareAttributes(initial.attributes(), newItem.attributes());

        // For relations, use the new node’s list if not empty; otherwise, fallback to the initial node’s relations.
        List<String> relations = !newItem.relations().isEmpty() ? newItem.relations() : initial.relations();
        boolean relationsChanged = !compareRelations(initial.relations(), newItem.relations());
        List<TreeItem> mergedChildren = mergeChildren(initial.children(), newItem.children());

        boolean attrChanged = comparedAttributes.stream().anyMatch(attr -> !"NO_ACTION".equals(attr.operation()));
        String op = (attrChanged || relationsChanged) ? "UPDATE" : "NO_ACTION";
        logger.info("Node {} compared: op set to {}", newItem.id(), op);

        return Optional.of(new TreeItem(newItem.id(), newItem.type(), op, comparedAttributes, mergedChildren, relations));
    }

    /**
     * Compares two lists of attributes by correlating them by name and preserving the desired order.
     * <p>
     * Order is preserved as follows:
     * <ul>
     *   <li>First, iterate over the new node's attributes (in their natural order):
     *       - If an attribute exists in the new node and also in the initial node, mark as NO_ACTION or UPDATE.
     *       - If it exists only in the new node, mark as CREATE.
     *   </li>
     *   <li>Then, for any attribute in the initial node that was not present in the new node, mark it as DELETE.</li>
     * </ul>
     *
     * @param initialAttrs the list of attributes from the initial node (never null)
     * @param newAttrs     the list of attributes from the new node (never null)
     * @return a list of compared attributes with operations set in the desired order
     */
    public static List<Attribute> compareAttributes(List<Attribute> initialAttrs, List<Attribute> newAttrs) {
        logger.debug("Comparing attributes: initialAttrs size={} vs newAttrs size={}",
                initialAttrs.size(), newAttrs.size());

        // Build a lookup for the initial node's attributes.
        Map<String, String> initMap = new HashMap<>();
        for (Attribute attr : initialAttrs) {
            initMap.put(attr.name(), attr.value());
        }

        List<Attribute> result = new ArrayList<>();

        // To keep track of which attributes we have processed from the new node.
        Set<String> processed = new HashSet<>();

        // Process attributes from the new node first, preserving their order.
        for (Attribute newAttr : newAttrs) {
            String name = newAttr.name();
            processed.add(name);

            if (initMap.containsKey(name)) {
                String initVal = initMap.get(name);
                String newVal = newAttr.value();
                String op = Objects.equals(initVal, newVal) ? "NO_ACTION" : "UPDATE";

                if ("UPDATE".equals(op)) {
                    logger.debug("Attribute '{}' updated from '{}' to '{}'", name, initVal, newVal);
                } else {
                    logger.debug("Attribute '{}' unchanged", name);
                }

                result.add(new Attribute(name, newVal, op));
            } else {
                logger.debug("Attribute '{}' created with value '{}'", name, newAttr.value());

                result.add(new Attribute(name, newAttr.value(), "CREATE"));
            }
        }

        // Process attributes that exist only in the initial node.
        for (Attribute initAttr : initialAttrs) {
            if (!processed.contains(initAttr.name())) {
                logger.debug("Attribute '{}' deleted; was '{}'", initAttr.name(), initAttr.value());
                result.add(new Attribute(initAttr.name(), initAttr.value(), "DELETE"));
            }
        }
        return result;
    }

    /**
     * Compares two relation lists (order-independent).
     *
     * @param initRel the relations from the initial node (never null)
     * @param newRel  the relations from the new node (never null)
     * @return true if the sets of relations are equal; false otherwise.
     */
    public static boolean compareRelations(List<String> initRel, List<String> newRel) {
        logger.debug("Comparing relations: initialRel={} vs newRel={}", initRel, newRel);
        return new HashSet<>(initRel).equals(new HashSet<>(newRel));
    }

    /**
     * Merges the children of two nodes by matching children on their identifier.
     *
     * @param initialChildren the list of children from the initial node (never null)
     * @param newChildren     the list of children from the new node (never null)
     * @return a merged list of TreeItem children with computed operations.
     */
    public static List<TreeItem> mergeChildren(List<TreeItem> initialChildren, List<TreeItem> newChildren) {
        logger.info("Merging children: initialChildren size={} vs newChildren size={}",
                initialChildren.size(), newChildren.size());

        Map<String, TreeItem> initMap = new HashMap<>();
        Map<String, TreeItem> newMap = new HashMap<>();

        for (TreeItem child : initialChildren) {
            initMap.put(child.id(), child);
        }
        for (TreeItem child : newChildren) {
            newMap.put(child.id(), child);
        }

        Set<String> allIds = new HashSet<>();
        allIds.addAll(initMap.keySet());
        allIds.addAll(newMap.keySet());

        List<TreeItem> merged = new ArrayList<>();
        for (String id : allIds) {
            TreeItem initChild = initMap.get(id);
            TreeItem newChild = newMap.get(id);
            Optional<TreeItem> comparedChild = compareNodes(initChild, newChild);

            comparedChild.ifPresent(child -> {
                logger.debug("Merged child {}: op={}", child.id(), child.operation());
                merged.add(child);
            });
        }

        merged.sort(Comparator.comparing(TreeItem::id));
        logger.info("Finished merging children. Total merged children: {}", merged.size());
        return merged;
    }

    /**
     * Clones a TreeItem (and its subtree) while setting every node's and attribute's operation to the specified op.
     *
     * @param node the TreeItem to clone (assumed non-null)
     * @param op   the operation to assign (e.g., CREATE or DELETE)
     * @return a cloned TreeItem with all operations set to op.
     */
    public static TreeItem cloneWithOperation(TreeItem node, String op) {
        logger.debug("Cloning node {} with op={}", node.id(), op);

        List<Attribute> attrs = node.attributes().stream()
                .map(attr -> new Attribute(attr.name(), attr.value(), op))
                .collect(Collectors.toList());

        List<TreeItem> children = node.children().stream()
                .map(child -> cloneWithOperation(child, op))
                .collect(Collectors.toList());

        return new TreeItem(node.id(), node.type(), op, attrs, children, node.relations());
    }
}
