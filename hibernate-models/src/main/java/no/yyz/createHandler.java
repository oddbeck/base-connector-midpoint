package no.yyz;

import no.yyz.models.models.User;
import org.identityconnectors.framework.common.objects.*;
import org.identityconnectors.framework.common.objects.filter.EqualsFilter;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class createHandler {

    public void create() {

//        User user = new User();
//        for (Attribute attribute : attributes) {
//            String name = attribute.getName();
//            List<Object> value = attribute.getValue();
//            Object firstValue = null;
//            if (!value.isEmpty()) {
//                firstValue = value.getFirst();
//            }
//            switch (name.toLowerCase()) {
//                case "email": {
//                    if (firstValue != null) {
//                        user.setEmail(firstValue.toString());
//                    }
//                    break;
//                }
//                case "username": {
//                    if (firstValue != null) {
//                        user.setUsername(firstValue.toString());
//                    }
//                    break;
//                }
//                case "givenname": {
//                    if (firstValue != null) {
//                        user.setGivenName(firstValue.toString());
//                    }
//                    break;
//                }
//                case "lastname": {
//                    if (firstValue != null) {
//                        user.setLastName(firstValue.toString());
//                    }
//                    break;
//                }
//                case "fullname": {
//                    if (firstValue != null) {
//                        user.setFullName(firstValue.toString());
//                    }
//                    break;
//                }
//                default:
//                    break;
//            }
//        }
//        try {
//            user = this.userservice.persist(user);
//        } catch (Exception e) {
//            throw new RuntimeException(e);
//        }
//        int userId = user.getId();
    }
}
//        try {
//                if (filter instanceof EqualsFilter equalsFilter) {
//var attributeName = equalsFilter.getAttribute();
//                if (attributeName.getName().equals(Uid.NAME)) {
//        for (var value : attributeName.getValue()) {
//var user = this.userservice.getById(Integer.parseInt(value.toString()));
//                        if (user != null) {
//Set<Attribute> attributes = new HashSet<>();
//                            attributes.add(AttributeBuilder.build("username", user.getUsername()));
//        attributes.add(AttributeBuilder.build("email", user.getEmail()));
//        attributes.add(AttributeBuilder.build(Uid.NAME, Integer.toString(user.getId())));
//        attributes.add(AttributeBuilder.build(Name.NAME, user.getEmail()));
//ConnectorObject obj = new ConnectorObject(ObjectClass.ACCOUNT, attributes);
//                            resultsHandler.handle(obj);
//                        }
//                                }
//                                return;
//                                }
//                                }
//List<User> list = this.userservice.getAll();
//            for (User user : list) {
//Set<Attribute> attributes = new HashSet<>();
//                attributes.add(AttributeBuilder.build("username", user.getUsername()));
//        attributes.add(AttributeBuilder.build("email", user.getEmail()));
//        attributes.add(AttributeBuilder.build(Uid.NAME, Integer.toString(user.getId())));
//        attributes.add(AttributeBuilder.build(Name.NAME, user.getEmail()));
//ConnectorObject obj = new ConnectorObject(ObjectClass.ACCOUNT, attributes);
//                resultsHandler.handle(obj);
//            }
//                    } catch (Exception e) {
//        throw new RuntimeException(e);
//        }
//
