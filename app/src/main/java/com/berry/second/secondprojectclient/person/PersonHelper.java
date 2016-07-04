package com.berry.second.secondprojectclient.person;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Helper class for providing sample content for user interfaces created by
 * Android template wizards.
 * <p/>
 * TODO: Replace all uses of this class before publishing your app.
 */
public class PersonHelper {

    /**
     * An array of sample (dummy) items.
     */
    public static final List<Person> mItems = new ArrayList<Person>();

    /**
     * A map of sample (dummy) items, by ID.
     */
    public static final Map<Integer, Person> mItemsMap = new HashMap<Integer, Person>();

    private static final int COUNT = 25;

    static {
        // Add some sample items.
        for (int i = 1; i <= COUNT; i++) {
            addItem(createPerson(i));
        }
    }

    private static void addItem(Person item) {
        mItems.add(item);
        mItemsMap.put(item.id, item);
    }

    private static Person createPerson(int position) {
        return new Person(Integer.valueOf(position),"emptyName","emptyEmail","emptyPhoneNumber");
    }

//    private static String makeDetails(int position) {
//        StringBuilder builder = new StringBuilder();
//        builder.append("Details about Item: ").append(position);
//        for (int i = 0; i < position; i++) {
//            builder.append("\nMore details information here.");
//        }
//        return builder.toString();
//    }

    /**
     * A dummy item representing a piece of content.
     */
    public static class Person {
        public final Integer id;
        public final String name;
        public final String email;
        public final String phone;

        public Person(Integer id, String name, String email, String phone) {
            this.id=id;
            this.name = name;
            this.email=email;
            this.phone=phone;
        }

        @Override
        public String toString() {
            return  "id : " + id.toString() +
                    "\tname : " + name +
                    "\temail : " + email +
                    "\tphone : " + phone;
        }
    }
}
