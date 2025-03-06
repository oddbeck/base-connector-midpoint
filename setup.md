# application role
On application role you must have something similar to this:
This is basically copied from the docs page from Evolveum

    <inducement >
        <construction>
            <resourceRef oid="YOUR RESOURCE OID" relation="org:default" type="c:ResourceType"/>
                <kind>account</kind>
                <intent>vanlig</intent> <!-- use your intent here -->
                <association id="10">
                    <ref>medlemsskap</ref> <!-- use your association here -->
                        <outbound>
                            <expression>
                                <associationFromLink>
                                    <projectionDiscriminator xsi:type="c:ShadowDiscriminatorType">
                                        <kind>entitlement</kind>
                                        <intent>medlemmer</intent> <!-- use your intent here -->
                                    </projectionDiscriminator>
                                </associationFromLink>
                            </expression>
                        </outbound>
                </association>
        </construction>
    <order>2</order>
    </inducement>

You might also need to have an inducement for your entitlements in the same resource

    <inducement id="7">
        <construction>
            <resourceRef oid="cfe026a6-d6f3-4992-aa2e-30f09d0b0d6a" relation="org:default" type="c:ResourceType"/>
            <kind>entitlement</kind>
            <intent>medlemmer</intent>
        </construction>
    </inducement>


# connector setup

This is the reference between the account and entitlements

        <configured xmlns:cap="http://midpoint.evolveum.com/xml/ns/public/resource/capabilities-3">
            <cap:references>
                <cap:enabled>true</cap:enabled>
                <cap:type id="52">
                    <cap:name>referansenavn</cap:name>
                    <cap:subject>
                        <cap:delineation id="53">
                            <cap:objectClass>ri:AccountObjectClass</cap:objectClass>
                        </cap:delineation>
                        <cap:primaryBindingAttributeRef>icfs:uid</cap:primaryBindingAttributeRef>
                        <cap:localItemName>medlemsskap</cap:localItemName>
                    </cap:subject>
                    <cap:object>
                        <cap:delineation id="54">
                            <cap:objectClass>ri:GroupObjectClass</cap:objectClass>
                        </cap:delineation>
                        <cap:primaryBindingAttributeRef>members</cap:primaryBindingAttributeRef>
                    </cap:object>
                    <cap:direction>objectToSubject</cap:direction>
                    <cap:explicitReferentialIntegrity>true</cap:explicitReferentialIntegrity>
                </cap:type>
            </cap:references>
        </configured>


Then you also need the associations

        <associationType id="58">
            <name>medlemsskap</name>
            <displayName>association medlemsskap</displayName>
            <subject>
                <objectType id="59">
                    <kind>account</kind>
                    <intent>vanlig</intent> <!-- use your intent here -->
                </objectType>
                <association>
                    <ref>medlemsskap</ref> <!-- use your association here -->
                    <sourceAttributeRef>medlemsskap</sourceAttributeRef>
                </association>
            </subject>
            <object id="60">
                <objectType id="61">
                    <kind>entitlement</kind>
                    <intent>medlemmer</intent> <!-- use your intent here -->
                </objectType>
            </object>
        </associationType>
