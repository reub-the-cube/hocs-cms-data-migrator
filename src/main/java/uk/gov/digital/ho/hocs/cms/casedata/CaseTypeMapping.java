package uk.gov.digital.ho.hocs.cms.casedata;

import java.util.HashMap;

public class CaseTypeMapping {

    public static String getCaseType(String owningCsu) {
        HashMap<String, String> caseTypeMapping = new HashMap<>();
        caseTypeMapping.put("CSU-Wales and South West", CaseType.COMP.name());
        caseTypeMapping.put("CSU-Crime Directorate", CaseType.COMP.name());
        caseTypeMapping.put("UKVI", CaseType.COMP.name());
        caseTypeMapping.put("CSU-Border Force", CaseType.BF.name());
        caseTypeMapping.put("CSU-Detention", CaseType.IEDET.name());
        caseTypeMapping.put("CSU-Immigration Enquiry Bureau", CaseType.COMP.name());
        caseTypeMapping.put("CCSU-EUSS", CaseType.COMP.name());
        caseTypeMapping.put("CSU-NE Yorkshire and Humber", CaseType.COMP.name());
        caseTypeMapping.put("CSU-Case Resolution Directorate", CaseType.COMP.name());
        caseTypeMapping.put("CSU-Sheffield Call Centre", CaseType.COMP.name());
        caseTypeMapping.put("CSU-Wales & South West", CaseType.COMP.name());
        caseTypeMapping.put("RH-NE Ombudsman ExG", CaseType.COMP.name());
        caseTypeMapping.put("CSU-Scotland and NI", CaseType.COMP.name());
        caseTypeMapping.put("HMPO", CaseType.POGR.name());
        caseTypeMapping.put("IE", CaseType.COMP.name());
        caseTypeMapping.put("Surge Team", CaseType.COMP.name());
        caseTypeMapping.put("CSU-Midland and East", CaseType.COMP.name());
        caseTypeMapping.put("CSU-NW Region", CaseType.COMP.name());
        caseTypeMapping.put("CSU-Criminal Casework Directorate", CaseType.COMP.name());
        caseTypeMapping.put("CSU-London And SE", CaseType.COMP.name());
        caseTypeMapping.put("RH-International", CaseType.COMP.name());
        caseTypeMapping.put("Asylum Protection Hub Pilot", CaseType.COMP.name());
        caseTypeMapping.put("UNKNOWN", null);
        caseTypeMapping.put("NULL", null);

        return caseTypeMapping.get(owningCsu);
    }

}
