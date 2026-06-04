package com.uzgram.messenger.data.remote

import com.uzgram.messenger.data.remote.dto.*
import retrofit2.http.*

/**
 * Retrofit API interface.
 * Base URL: https://uzgram.online/api/v1/
 *
 * This is the primary ApiService used by all repository implementations.
 */
interface ApiService {

    // ── Auth ────────────────────────────────────────────────────────────────
    @POST("auth/register")
    suspend fun register(@Body body: Map<String, String>): AuthResponseDto

    @POST("auth/login")
    suspend fun login(@Body body: Map<String, String>): AuthResponseDto

    @POST("auth/refresh")
    suspend fun refreshToken(@Body body: Map<String, String>): AuthResponseDto

    @POST("auth/logout")
    suspend fun logout(): Unit

    @POST("auth/verify-email")
    suspend fun verifyEmail(@Body body: Map<String, String>): Map<String, Any>

    @POST("auth/forgot-password")
    suspend fun forgotPassword(@Body body: Map<String, String>): Map<String, Any>

    @POST("auth/reset-password")
    suspend fun resetPassword(@Body body: Map<String, String>): Map<String, Any>

    @POST("auth/change-password")
    suspend fun changePassword(@Body body: Map<String, String>): Map<String, Any>

    // ── Users ───────────────────────────────────────────────────────────────
    @GET("users/me")
    suspend fun getMe(): UserDto

    @GET("users/online")
    suspend fun getOnlineContacts(): List<UserDto>

    @GET("users/search")
    suspend fun searchUsers(@Query("q") query: String): List<UserDto>

    @GET("users/{userId}")
    suspend fun getUserById(@Path("userId") userId: String): UserDto

    @GET("users/by-username/{username}")
    suspend fun getUserByUsername(@Path("username") username: String): UserDto

    @PUT("users/me")
    suspend fun updateProfile(@Body body: Map<String, Any?>): UserDto

    @POST("users/{userId}/block")
    suspend fun blockUser(@Path("userId") userId: String): Unit

    @DELETE("users/{userId}/block")
    suspend fun unblockUser(@Path("userId") userId: String): Unit

    @GET("users/blocked")
    suspend fun getBlockedUsers(): List<UserDto>

    @POST("users/contacts")
    suspend fun addContact(@Body body: Map<String, String>): Unit

    @DELETE("users/contacts/{userId}")
    suspend fun removeContact(@Path("userId") userId: String): Unit

    @GET("users/contacts")
    suspend fun getContacts(): List<UserDto>

    @POST("users/contacts/sync")
    suspend fun syncContacts(@Body body: Map<String, List<String>>): List<UserDto>

    @POST("users/{userId}/report")
    suspend fun reportUser(
        @Path("userId") userId: String,
        @Body body: Map<String, String>
    ): Unit

    // ── Chats ───────────────────────────────────────────────────────────────
    @GET("chats")
    suspend fun getChats(): List<ChatDto>

    @GET("chats/{chatId}")
    suspend fun getChatById(@Path("chatId") chatId: String): ChatDto

    @POST("chats")
    suspend fun createChat(@Body body: Map<String, Any>): ChatDto

    @POST("chats/group")
    suspend fun createGroupChat(@Body body: Map<String, Any>): ChatDto

    @PUT("chats/{chatId}")
    suspend fun updateChat(
        @Path("chatId") chatId: String,
        @Body body: Map<String, Any?>
    ): ChatDto

    @DELETE("chats/{chatId}")
    suspend fun deleteChat(@Path("chatId") chatId: String): Unit

    @POST("chats/{chatId}/archive")
    suspend fun archiveChat(@Path("chatId") chatId: String): Unit

    @DELETE("chats/{chatId}/archive")
    suspend fun unarchiveChat(@Path("chatId") chatId: String): Unit

    @POST("chats/{chatId}/pin")
    suspend fun pinChat(@Path("chatId") chatId: String): Unit

    @DELETE("chats/{chatId}/pin")
    suspend fun unpinChat(@Path("chatId") chatId: String): Unit

    @POST("chats/{chatId}/mute")
    suspend fun muteChat(
        @Path("chatId") chatId: String,
        @Body body: Map<String, Any>
    ): Unit

    @DELETE("chats/{chatId}/mute")
    suspend fun unmuteChat(@Path("chatId") chatId: String): Unit

    @POST("chats/{chatId}/members")
    suspend fun addMembers(
        @Path("chatId") chatId: String,
        @Body body: Map<String, List<String>>
    ): Unit

    @DELETE("chats/{chatId}/members/{userId}")
    suspend fun removeMember(
        @Path("chatId") chatId: String,
        @Path("userId") userId: String
    ): Unit

    @POST("chats/{chatId}/leave")
    suspend fun leaveChat(@Path("chatId") chatId: String): Unit

    @POST("chats/{chatId}/read")
    suspend fun markChatRead(@Path("chatId") chatId: String): Unit

    // ── Messages ────────────────────────────────────────────────────────────
    @GET("chats/{chatId}/messages")
    suspend fun getMessages(
        @Path("chatId") chatId: String,
        @Query("before") before: String? = null,
        @Query("limit") limit: Int = 50
    ): List<MessageDto>

    @POST("chats/{chatId}/messages")
    suspend fun sendMessage(
        @Path("chatId") chatId: String,
        @Body body: Map<String, Any?>
    ): MessageDto

    @PUT("chats/{chatId}/messages/{messageId}")
    suspend fun editMessage(
        @Path("chatId") chatId: String,
        @Path("messageId") messageId: String,
        @Body body: Map<String, String>
    ): MessageDto

    @DELETE("chats/{chatId}/messages/{messageId}")
    suspend fun deleteMessage(
        @Path("chatId") chatId: String,
        @Path("messageId") messageId: String
    ): Unit

    @POST("chats/{chatId}/messages/{messageId}/forward")
    suspend fun forwardMessage(
        @Path("chatId") chatId: String,
        @Path("messageId") messageId: String,
        @Body body: Map<String, String>
    ): MessageDto

    @POST("chats/{chatId}/messages/{messageId}/react")
    suspend fun reactToMessage(
        @Path("chatId") chatId: String,
        @Path("messageId") messageId: String,
        @Body body: Map<String, String>
    ): Unit

    @POST("chats/{chatId}/messages/{messageId}/read")
    suspend fun markMessageRead(
        @Path("chatId") chatId: String,
        @Path("messageId") messageId: String
    ): Unit

    @POST("chats/{chatId}/typing")
    suspend fun sendTypingIndicator(@Path("chatId") chatId: String): Unit

    // ── Stories ─────────────────────────────────────────────────────────────
    @GET("stories/feed")
    suspend fun getStoryFeed(): List<StoryGroupDto>

    @GET("stories/me")
    suspend fun getMyStories(): List<StoryDto>

    @GET("stories/user/{userId}")
    suspend fun getUserStories(@Path("userId") userId: String): List<StoryDto>

    @POST("stories")
    suspend fun createStory(@Body body: CreateStoryDto): StoryDto

    @DELETE("stories/{storyId}")
    suspend fun deleteStory(@Path("storyId") storyId: String): Unit

    @POST("stories/{storyId}/view")
    suspend fun viewStory(@Path("storyId") storyId: String): Unit

    @POST("stories/{storyId}/react")
    suspend fun reactToStory(
        @Path("storyId") storyId: String,
        @Body body: Map<String, String>
    ): Unit

    @POST("stories/{storyId}/reply")
    suspend fun replyToStory(
        @Path("storyId") storyId: String,
        @Body body: Map<String, String>
    ): Unit

    @GET("stories/highlights/{userId}")
    suspend fun getHighlights(@Path("userId") userId: String): List<HighlightDto>

    @POST("stories/highlights")
    suspend fun createHighlight(@Body body: Map<String, Any?>): HighlightDto

    @POST("stories/highlights/{highlightId}/stories")
    suspend fun addToHighlight(
        @Path("highlightId") highlightId: String,
        @Body body: Map<String, String>
    ): Unit

    @DELETE("stories/highlights/{highlightId}")
    suspend fun deleteHighlight(@Path("highlightId") highlightId: String): Unit

    // ── Calls ───────────────────────────────────────────────────────────────
    @GET("calls")
    suspend fun getCallHistory(): List<Map<String, Any?>>

    @GET("calls/{callId}")
    suspend fun getCallById(@Path("callId") callId: String): Map<String, Any?>

    @DELETE("calls/{callId}")
    suspend fun deleteCallRecord(@Path("callId") callId: String): Unit

    @DELETE("calls")
    suspend fun clearCallHistory(): Unit

    @GET("calls/missed/count")
    suspend fun getMissedCallCount(): Map<String, Any>

    @POST("calls/initiate")
    suspend fun initiateCall(@Body body: Map<String, String>): Map<String, Any?>

    @POST("calls/{callId}/accept")
    suspend fun acceptCall(@Path("callId") callId: String): Unit

    @POST("calls/{callId}/decline")
    suspend fun declineCall(@Path("callId") callId: String): Unit

    @POST("calls/{callId}/end")
    suspend fun endCall(
        @Path("callId") callId: String,
        @Body body: Map<String, Int>
    ): Unit

    @POST("calls/{callId}/signal")
    suspend fun sendCallSignal(
        @Path("callId") callId: String,
        @Body body: Map<String, Any>
    ): Unit

    // ── Mini Apps ───────────────────────────────────────────────────────────
    @GET("mini-apps")
    suspend fun getMiniApps(
        @Query("category") category: String? = null,
        @Query("limit") limit: Int = 50,
        @Query("offset") offset: Int = 0
    ): List<MiniAppDto>

    @GET("mini-apps/featured")
    suspend fun getFeaturedMiniApps(): List<MiniAppDto>

    @GET("mini-apps/installed")
    suspend fun getInstalledMiniApps(): List<MiniAppDto>

    @GET("mini-apps/search")
    suspend fun searchMiniApps(@Query("q") query: String): List<MiniAppDto>

    @GET("mini-apps/{appId}")
    suspend fun getMiniAppById(@Path("appId") appId: String): MiniAppDto

    @POST("mini-apps/{appId}/install")
    suspend fun installMiniApp(@Path("appId") appId: String): Unit

    @DELETE("mini-apps/{appId}/install")
    suspend fun uninstallMiniApp(@Path("appId") appId: String): Unit

    @POST("mini-apps/{appId}/rate")
    suspend fun rateMiniApp(
        @Path("appId") appId: String,
        @Body body: Map<String, Any?>
    ): Unit

    @POST("mini-apps/{appId}/report")
    suspend fun reportMiniApp(
        @Path("appId") appId: String,
        @Body body: Map<String, String>
    ): Unit

    // ── Admin ───────────────────────────────────────────────────────────────
    @GET("admin/stats")
    suspend fun getAdminStats(): Map<String, Any>

    @GET("admin/users")
    suspend fun getAdminUsers(
        @Query("page") page: Int = 1,
        @Query("limit") limit: Int = 50,
        @Query("q") query: String? = null
    ): Map<String, Any>

    @POST("admin/users/{userId}/ban")
    suspend fun banUser(@Path("userId") userId: String): Unit

    @POST("admin/users/{userId}/unban")
    suspend fun unbanUser(@Path("userId") userId: String): Unit

    @POST("admin/users/{userId}/verify")
    suspend fun verifyUser(@Path("userId") userId: String): Unit

    @GET("admin/reports")
    suspend fun getAdminReports(@Query("page") page: Int = 1): Map<String, Any>

    @POST("admin/reports/{reportId}/resolve")
    suspend fun resolveReport(@Path("reportId") reportId: String): Unit

    @POST("admin/notifications/broadcast")
    suspend fun broadcastNotification(@Body body: Map<String, String>): Unit
}
