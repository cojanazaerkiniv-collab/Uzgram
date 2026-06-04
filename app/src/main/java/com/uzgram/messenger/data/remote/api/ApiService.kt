package com.uzgram.messenger.data.remote.api

import com.uzgram.messenger.data.remote.dto.*
import retrofit2.http.*

interface ApiService {

    // ── AUTH ──────────────────────────────────────────────────────────────
    @POST("auth/register")
    suspend fun register(@Body body: RegisterRequest): AuthResponse

    @POST("auth/login")
    suspend fun login(@Body body: LoginRequest): AuthResponse

    @POST("auth/google")
    suspend fun loginWithGoogle(@Body body: Map<String, String>): AuthResponse

    @POST("auth/logout")
    suspend fun logout(): SuccessResponse

    @POST("auth/refresh")
    suspend fun refreshToken(@Body body: RefreshTokenRequest): AuthResponse

    @POST("auth/forgot-password")
    suspend fun forgotPassword(@Body body: ForgotPasswordRequest): SuccessResponse

    @POST("auth/reset-password")
    suspend fun resetPassword(@Body body: ResetPasswordRequest): SuccessResponse

    @POST("auth/verify-email")
    suspend fun verifyEmail(@Body body: Map<String, String>): SuccessResponse

    @POST("auth/resend-verification")
    suspend fun resendVerification(@Body body: Map<String, String>): SuccessResponse

    // ── USERS ─────────────────────────────────────────────────────────────
    @GET("users/me")
    suspend fun getMe(): UserDto

    @PUT("users/me")
    suspend fun updateProfile(@Body body: Map<String, Any?>): UserDto

    @GET("users/me/online-contacts")
    suspend fun getOnlineContacts(): List<UserDto>

    @GET("users/search")
    suspend fun searchUsers(
        @Query("q") query: String,
        @Query("limit") limit: Int = 20
    ): List<UserDto>

    @GET("users/{userId}")
    suspend fun getUserById(@Path("userId") userId: String): UserDto

    @GET("users/by-username/{username}")
    suspend fun getUserByUsername(@Path("username") username: String): UserDto

    @GET("users/contacts")
    suspend fun getContacts(): List<UserDto>

    @POST("users/contacts")
    suspend fun addContact(@Body body: Any): SuccessResponse

    @DELETE("users/contacts/{contactId}")
    suspend fun removeContact(@Path("contactId") contactId: String): SuccessResponse

    @POST("users/contacts/sync")
    suspend fun syncContacts(@Body body: Map<String, Any>): List<UserDto>

    @POST("users/block/{userId}")
    suspend fun blockUser(@Path("userId") userId: String): SuccessResponse

    @DELETE("users/block/{userId}")
    suspend fun unblockUser(@Path("userId") userId: String): SuccessResponse

    @GET("users/blocked")
    suspend fun getBlockedUsers(): List<UserDto>

    @POST("users/{userId}/report")
    suspend fun reportUser(@Path("userId") userId: String, @Body body: Map<String, String>): SuccessResponse

    // ── CHATS ─────────────────────────────────────────────────────────────
    @GET("chats")
    suspend fun getChats(
        @Query("limit") limit: Int = 30,
        @Query("cursor") cursor: String? = null
    ): ChatListResponse

    @POST("chats")
    suspend fun createChat(@Body body: CreateChatRequest): ChatDto

    @POST("chats/or-create")
    suspend fun getOrCreatePrivateChat(@Body body: GetOrCreateChatRequest): ChatDto

    @GET("chats/{chatId}")
    suspend fun getChatById(@Path("chatId") chatId: String): ChatDto

    @PUT("chats/{chatId}")
    suspend fun updateChat(@Path("chatId") chatId: String, @Body body: UpdateChatRequest): ChatDto

    @DELETE("chats/{chatId}")
    suspend fun leaveChat(@Path("chatId") chatId: String): SuccessResponse

    @GET("chats/{chatId}/members")
    suspend fun getChatMembers(@Path("chatId") chatId: String): List<ChatMemberDto>

    @POST("chats/{chatId}/members")
    suspend fun addChatMembers(@Path("chatId") chatId: String, @Body body: AddMembersRequest): SuccessResponse

    @DELETE("chats/{chatId}/members/{memberId}")
    suspend fun removeChatMember(@Path("chatId") chatId: String, @Path("memberId") memberId: String): SuccessResponse

    @GET("chats/stats/summary")
    suspend fun getChatsSummary(): ChatsSummaryDto

    @POST("chats/{chatId}/mute")
    suspend fun muteChat(@Path("chatId") chatId: String): SuccessResponse

    @POST("chats/{chatId}/unmute")
    suspend fun unmuteChat(@Path("chatId") chatId: String): SuccessResponse

    @POST("chats/{chatId}/archive")
    suspend fun archiveChat(@Path("chatId") chatId: String): SuccessResponse

    @POST("chats/{chatId}/unarchive")
    suspend fun unarchiveChat(@Path("chatId") chatId: String): SuccessResponse

    // ── MESSAGES ──────────────────────────────────────────────────────────
    @GET("chats/{chatId}/messages")
    suspend fun getMessages(
        @Path("chatId") chatId: String,
        @Query("limit") limit: Int = 50,
        @Query("before") before: String? = null
    ): MessageListResponse

    @POST("chats/{chatId}/messages")
    suspend fun sendMessage(@Path("chatId") chatId: String, @Body body: SendMessageRequest): MessageDto

    @PUT("messages/{messageId}")
    suspend fun editMessage(@Path("messageId") messageId: String, @Body body: EditMessageRequest): MessageDto

    @DELETE("messages/{messageId}")
    suspend fun deleteMessage(@Path("messageId") messageId: String): SuccessResponse

    @POST("messages/{messageId}/react")
    suspend fun reactToMessage(@Path("messageId") messageId: String, @Body body: ReactRequest): MessageDto

    @POST("messages/{messageId}/read")
    suspend fun markMessageRead(@Path("messageId") messageId: String): SuccessResponse

    @POST("messages/search")
    suspend fun searchMessages(@Body body: SearchMessagesRequest): MessageListResponse

    @POST("chats/{chatId}/pin/{messageId}")
    suspend fun pinMessage(@Path("chatId") chatId: String, @Path("messageId") messageId: String): SuccessResponse

    // ── STORIES ───────────────────────────────────────────────────────────
    @GET("stories/feed")
    suspend fun getStoryFeed(): List<StoryGroupDto>

    @GET("stories/me")
    suspend fun getMyStories(): List<StoryDto>

    @GET("stories/user/{userId}")
    suspend fun getUserStories(@Path("userId") userId: String): List<StoryDto>

    @POST("stories")
    suspend fun createStory(@Body body: CreateStoryDto): StoryDto

    @POST("stories/{storyId}/view")
    suspend fun viewStory(@Path("storyId") storyId: String): SuccessResponse

    @POST("stories/{storyId}/react")
    suspend fun reactToStory(@Path("storyId") storyId: String, @Body body: Map<String, String>): SuccessResponse

    @POST("stories/{storyId}/reply")
    suspend fun replyToStory(@Path("storyId") storyId: String, @Body body: Map<String, String>): SuccessResponse

    @DELETE("stories/{storyId}")
    suspend fun deleteStory(@Path("storyId") storyId: String): SuccessResponse

    @GET("stories/highlights/{userId}")
    suspend fun getHighlights(@Path("userId") userId: String): List<HighlightDto>

    @POST("stories/highlights")
    suspend fun createHighlight(@Body body: Map<String, Any?>): HighlightDto

    @POST("stories/highlights/{highlightId}/items")
    suspend fun addToHighlight(@Path("highlightId") highlightId: String, @Body body: Map<String, String>): SuccessResponse

    @DELETE("stories/highlights/{highlightId}")
    suspend fun deleteHighlight(@Path("highlightId") highlightId: String): SuccessResponse

    // ── CALLS ─────────────────────────────────────────────────────────────
    @GET("calls")
    suspend fun getCallHistory(@Query("limit") limit: Int = 50): List<Map<String, Any?>>

    @GET("calls/{callId}")
    suspend fun getCallById(@Path("callId") callId: String): Map<String, Any?>

    @DELETE("calls/{callId}")
    suspend fun deleteCallRecord(@Path("callId") callId: String): SuccessResponse

    @DELETE("calls")
    suspend fun clearCallHistory(): SuccessResponse

    @GET("calls/missed/count")
    suspend fun getMissedCallCount(): Map<String, Any?>

    @POST("calls/initiate")
    suspend fun initiateCall(@Body body: Map<String, Any>): Map<String, Any?>

    @POST("calls/{callId}/accept")
    suspend fun acceptCall(@Path("callId") callId: String): SuccessResponse

    @POST("calls/{callId}/decline")
    suspend fun declineCall(@Path("callId") callId: String): SuccessResponse

    @POST("calls/{callId}/end")
    suspend fun endCall(@Path("callId") callId: String, @Body body: Map<String, Any>): SuccessResponse

    @POST("calls/{callId}/signal")
    suspend fun sendSignal(@Path("callId") callId: String, @Body body: Map<String, Any>): SuccessResponse

    // ── MINI APPS ─────────────────────────────────────────────────────────
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
    suspend fun installMiniApp(@Path("appId") appId: String): SuccessResponse

    @DELETE("mini-apps/{appId}/install")
    suspend fun uninstallMiniApp(@Path("appId") appId: String): SuccessResponse

    @POST("mini-apps/{appId}/rate")
    suspend fun rateMiniApp(@Path("appId") appId: String, @Body body: Map<String, Any?>): SuccessResponse

    @POST("mini-apps/{appId}/report")
    suspend fun reportMiniApp(@Path("appId") appId: String, @Body body: Map<String, String>): SuccessResponse

    // ── ADMIN ─────────────────────────────────────────────────────────────
    @GET("admin/stats")
    suspend fun getAdminStats(): AdminStatsDto

    @GET("admin/users")
    suspend fun getAdminUsers(
        @Query("q") query: String? = null,
        @Query("limit") limit: Int = 50,
        @Query("offset") offset: Int = 0,
        @Query("filter") filter: String = "all"
    ): AdminUsersResponse

    @POST("admin/users/{userId}/ban")
    suspend fun banUser(@Path("userId") userId: String, @Body body: BanUserRequest): SuccessResponse

    @POST("admin/users/{userId}/unban")
    suspend fun unbanUser(@Path("userId") userId: String): SuccessResponse

    @POST("admin/users/{userId}/verify")
    suspend fun verifyUser(@Path("userId") userId: String): SuccessResponse

    @GET("admin/chats")
    suspend fun getAdminChats(
        @Query("q") query: String? = null,
        @Query("limit") limit: Int = 50,
        @Query("offset") offset: Int = 0,
        @Query("type") type: String = "all"
    ): AdminChatsResponse

    @GET("admin/messages/recent")
    suspend fun getRecentMessages(@Query("limit") limit: Int = 50): List<MessageDto>

    @GET("admin/mini-apps")
    suspend fun getAdminMiniApps(
        @Query("limit") limit: Int = 50,
        @Query("offset") offset: Int = 0
    ): List<MiniAppDto>

    @POST("admin/mini-apps")
    suspend fun createMiniApp(@Body body: Map<String, Any>): MiniAppDto

    @PUT("admin/mini-apps/{appId}")
    suspend fun updateMiniApp(@Path("appId") appId: String, @Body body: Map<String, Any>): MiniAppDto

    @DELETE("admin/mini-apps/{appId}")
    suspend fun deleteMiniApp(@Path("appId") appId: String): SuccessResponse

    @POST("admin/mini-apps/{appId}/feature")
    suspend fun featureMiniApp(@Path("appId") appId: String): SuccessResponse

    @DELETE("admin/mini-apps/{appId}/feature")
    suspend fun unfeatureMiniApp(@Path("appId") appId: String): SuccessResponse
}
