package com.sky.csc.title.discovery.util;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.junit4.SpringRunner;

import static com.sky.csc.title.discovery.util.JsonUtils.Constants.TERMS_JSON_PROPERTY;
import static org.assertj.core.api.Assertions.assertThat;

@RunWith(SpringRunner.class)
public class JsonUtilsTest {


    private JsonUtils jsonUtils = new JsonUtils(new ObjectMapper());

    @Test
    public void testFormatToCSVArrayTermsSuccess() {

        String termFragmentsWithNoDelimiter =
                jsonUtils.getJsonContent("fragments/term-fragment-1.json").toString().concat(
                        jsonUtils.getJsonContent("fragments/term-fragment-2.json").toString());

        String actual = jsonUtils.formatToCSVArray(termFragmentsWithNoDelimiter);

        String expected = jsonUtils.getJsonContent("expected/commaDelimitedTermArray.json").toString();

        assertThat(actual).isEqualTo(expected);

    }

    @Test
    public void testFormatToCSVArrayOffersSuccess() {

        String offerTermFragmentsWithNoDelimiter =
                jsonUtils.getJsonContent("fragments/offer-term-fragment-1.json").toString().concat(
                        jsonUtils.getJsonContent("fragments/offer-term-fragment-2.json").toString());

        String actual = jsonUtils.formatToCSVArray(offerTermFragmentsWithNoDelimiter);

        String expected = jsonUtils.getJsonContent("expected/commaDelimitedOfferAndTermsArray.json").toString();

        assertThat(actual).isEqualTo(expected);

    }

    @Test
    public void testCombineParentWithChildrenNodesSuccess() {

        String termArrayChild = jsonUtils.getJsonContent("fragments/term-array.json").toString();

        String offerParent = jsonUtils.getJsonContent("fragments/offer-fragment.json").toString();

        final String actual = jsonUtils.combineParentWithChildrenNodes(offerParent, termArrayChild, TERMS_JSON_PROPERTY);

        String expected = jsonUtils.getJsonContent("expected/offerAndTermArrayCombined.json").toString();

        assertThat(actual).isEqualTo(expected);

    }

    @Test
    public void ignoreNullChildrenNodes() {

        String offerParent = jsonUtils.getJsonContent("fragments/offer-fragment.json").toString();

        String actual = jsonUtils.combineParentWithChildrenNodes(offerParent, null, TERMS_JSON_PROPERTY);

        String expected = jsonUtils.getJsonContent("expected/offerNoTerms.json").toString();

        assertThat(actual).isEqualTo(expected);

    }

    @Test
    public void testCombineParentWithChildrenNodesIgnoreNullParentNodes() {

        jsonUtils.combineParentWithChildrenNodes(null, null, TERMS_JSON_PROPERTY);

    }
}