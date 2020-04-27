package com.custom.acl.test;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;

/**
 * Utility class for resolving tests asserts depending on user role
 * For some reason Kotlin version of resolver provides instability during execution
 * of tests. Sometime it leads to problem with method invocation in org.jsmart:zerocode-tdd library
 *
 */
public class RoleResolver {

    private static final String ADMIN = "ADMIN";
    private static final String USER = "USER";
    private static final String REVIEWER = "REVIEWER";
    private static final String[] ROLES = {USER, REVIEWER, ADMIN};

    private static final Map<String, Integer> adminOperation = new HashMap<String, Integer>();
    private static final Map<String, Integer> reviewerOperation = new HashMap<String, Integer>();

    static {
        reviewerOperation.put(ADMIN, 200);
        reviewerOperation.put(REVIEWER, 200);
        reviewerOperation.put(USER, 403);

        adminOperation.put(ADMIN, 200);
        adminOperation.put(REVIEWER, 403);
        adminOperation.put(USER, 403);
    }

    public Map<String, Object> generateRoleForUnpublished() {
        var map = new HashMap<String, Object>();
        var index = new Random().nextInt(ROLES.length);
        map.put("role", ROLES[index]);
        map.put("status", reviewerOperation.get(ROLES[index]));
        return map;
    }

    public Map<String, Object> generateRoleForPublish() {
        var map = new HashMap<String, Object>();
        var index = new Random().nextInt(ROLES.length);
        map.put("role", ROLES[index]);
        map.put("status", reviewerOperation.get(ROLES[index]));
        return map;
    }

    public Map<String, Object>  generateRoleForDelete(){
        var map = new HashMap<String, Object>();
        var index = new Random().nextInt(ROLES.length);
        map.put("role", ROLES[index]);
        map.put("status", adminOperation.get(ROLES[index]));
        return map;
    }

    public Map<String, Object> generateRoleForGet() {
        var map = new HashMap<String, Object>();
        var index = new Random().nextInt(ROLES.length);
        map.put("role", ROLES[index]);
        map.put("status", reviewerOperation.get(ROLES[index]));
        return map;
    }
}
