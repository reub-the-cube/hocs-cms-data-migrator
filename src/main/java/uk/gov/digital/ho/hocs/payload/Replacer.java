package uk.gov.digital.ho.hocs.payload;

import lombok.Getter;

@Getter
public class Replacer {

    private final String[] searchList = {
            "@@TODAY@@",
            "@@COMPLAINT_TEXT@@",
            "@@APPLICANT_NAME@@",
            "@@AGENT_NAME@@",
            "@@NATIONALITY@@",
            "@@COUNTRY@@",
            "@@CITY@@",
            "@@DOB@@",
            "@@APPLICANT_EMAIL@@",
            "@@AGENT_EMAIL@@",
            "@@PHONE@@",
            "@@REFERENCE@@"
    };

    public String[] getReplaceList() {
        String[] replaceList = new String[searchList.length];
        for (int i = 0; i < searchList.length; i++) {
            String token = searchList[i];
            replaceList[i] = TokenReplacer.replaceToken(token);
        }
        return replaceList;
    }
}
