package com.springvuegradle.seng302team600.controller;

import com.springvuegradle.seng302team600.model.Activity;
import com.springvuegradle.seng302team600.model.FeedEvent;
import com.springvuegradle.seng302team600.model.User;
import com.springvuegradle.seng302team600.repository.ActivityRepository;
import com.springvuegradle.seng302team600.repository.FeedEventRepository;
import com.springvuegradle.seng302team600.service.UserAuthenticationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.web.server.ResponseStatusException;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(FeedEventController.class)
public class FeedEventControllerTest {

    @MockBean
    private UserAuthenticationService userAuthenticationService;
    @MockBean
    private FeedEventRepository feedEventRepository;
    @MockBean
    private ActivityRepository activityRepository;
    @Autowired
    private MockMvc mvc;

    private static final Long USER_ID_1 = 1L;
    private static final Long USER_ID_2 = 2L;
    private User dummyUser1;
    private User dummyUser2;
    private final String validToken = "valid";
    private static final Long ACTIVITY_ID_1 = 1L;
    private Activity dummyActivity;
    private List<FeedEvent> feedEventTable;



    @BeforeEach
    void setUp() {
        MockitoAnnotations.initMocks(this);
        dummyUser1 = new User();
        dummyUser1.setFirstName("John");
        ReflectionTestUtils.setField(dummyUser1, "userId", USER_ID_1);
        dummyUser2 = new User();
        dummyUser2.setFirstName("Douglas");
        ReflectionTestUtils.setField(dummyUser2, "userId", USER_ID_2);

        dummyActivity = new Activity();
        dummyActivity.setParticipants(new HashSet<>());
        ReflectionTestUtils.setField(dummyActivity, "activityId", ACTIVITY_ID_1);

        feedEventTable = new ArrayList<>();


        // Mocking UserAuthenticationService
        when(userAuthenticationService.findByUserId(Mockito.any(), Mockito.any(Long.class))).thenAnswer(i -> {
            Long id = i.getArgument(1);
            if (id.equals(USER_ID_1)) {
                return dummyUser1;
            } else if (id.equals(USER_ID_2)) {
                return dummyUser2;
            } else {
                throw new ResponseStatusException(HttpStatus.FORBIDDEN);
            }
        });
        when(userAuthenticationService.hasAdminPrivileges(Mockito.any())).thenAnswer(i ->
                ((User) i.getArgument(0)).getRole() >= 10);

        // Mocking FeedEventRepository
        when(feedEventRepository.findByViewerIdOrderByTimeStamp(Mockito.anyLong())).thenAnswer(i -> {
            Long id = i.getArgument(0);
            List<FeedEvent> result = new ArrayList<>();
            for (FeedEvent feedEvent : feedEventTable) {
                if (feedEvent.getViewerId().equals(id)) {
                    result.add(feedEvent);
                }
            }
            return result.isEmpty() ? null : result;
        });

        // Mocking ActivityRepository
        when(activityRepository.findByActivityId(Mockito.anyLong())).thenAnswer(i -> {
            Long id = i.getArgument(0);
            if (id.equals(ACTIVITY_ID_1)) {
                return dummyActivity;
            } else {
                return null;
            }
        });
    }

    /**
     * Test successful creation of new activity.
     */
    @Test
    void followWhenNotParticipant_succeed() throws Exception {

        MockHttpServletRequestBuilder httpReq = MockMvcRequestBuilders.post(
                "/profiles/{profileId}/subscriptions/activities/{activityId}", USER_ID_1, ACTIVITY_ID_1)
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON);

        MvcResult result = mvc.perform(httpReq)
                .andExpect(status().isOk())
                .andReturn();

        assertNotNull(result.getResponse());
    }

    /**
     * Test successful creation of new activity.
     */
    @Test
    void followWhenParticipant_fail() throws Exception {

        dummyActivity.addParticipant(dummyUser1);

        MockHttpServletRequestBuilder httpReq = MockMvcRequestBuilders.post(
                "/profiles/{profileId}/subscriptions/activities/{activityId}", USER_ID_1, ACTIVITY_ID_1)
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON);

        MvcResult result = mvc.perform(httpReq)
                .andExpect(status().isBadRequest())
                .andReturn();

        assertNotNull(result.getResponse());
        assertEquals(
                "User can't re-follow an event they're currently participating in.",
                result.getResponse().getErrorMessage());
    }

    /**
     * Test successful creation of new activity.
     */
    @Test
    void unFollowWhenParticipant_succeed() throws Exception {

        dummyActivity.addParticipant(dummyUser1);

        MockHttpServletRequestBuilder httpReq = MockMvcRequestBuilders.delete(
                "/profiles/{profileId}/subscriptions/activities/{activityId}", USER_ID_1, ACTIVITY_ID_1)
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON);

        MvcResult result = mvc.perform(httpReq)
                .andExpect(status().isOk())
                .andReturn();

        assertNotNull(result.getResponse());
    }

    /**
     * Test successful creation of new activity.
     */
    @Test
    void unFollowWhenNotParticipant_fail() throws Exception {

        MockHttpServletRequestBuilder httpReq = MockMvcRequestBuilders.delete(
                "/profiles/{profileId}/subscriptions/activities/{activityId}", USER_ID_1, ACTIVITY_ID_1)
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON);

        MvcResult result = mvc.perform(httpReq)
                .andExpect(status().isBadRequest())
                .andReturn();

        assertEquals(
                "User can't un-follow an event they're not participating in.",
                result.getResponse().getErrorMessage());
    }

}
