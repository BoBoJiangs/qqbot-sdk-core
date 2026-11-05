package top.sshh.qqbot.sdk;

import java.util.Map;

/** All REST operations in the committed official documentation snapshot. */
public final class QQOpenApi {
    private final ApiClient client;
    public QQOpenApi(ApiClient client) { this.client = java.util.Objects.requireNonNull(client); }
    public ApiResponse call(ApiEndpoint endpoint, Map<String, String> path, ApiRequest request) {
        return client.execute(endpoint, path, request);
    }

    /** 删除子频道. @see <a href="https://bot.q.qq.com/wiki/develop/api-v2/autogen/api/channels_channel_id.delete.html">Official documentation</a> */
    public ApiResponse deleteChannelsChannelId(String channel_id, ApiRequest request) {
        return call(ApiEndpoint.DELETE_CHANNELS_CHANNEL_ID, Map.of("channel_id", channel_id), request);
    }

    /** 删除子频道公告（2022年3月15日后废弃）. @see <a href="https://bot.q.qq.com/wiki/develop/api-v2/server-inter/channel/content/announces/delete_channel_announces.html">Official documentation</a> */
    public ApiResponse deleteChannelsChannelIdAnnouncesMessageId(String channel_id, String message_id, ApiRequest request) {
        return call(ApiEndpoint.DELETE_CHANNELS_CHANNEL_ID_ANNOUNCES_MESSAGE_ID, Map.of("channel_id", channel_id, "message_id", message_id), request);
    }

    /** 撤回子频道消息. @see <a href="https://bot.q.qq.com/wiki/develop/api-v2/server-inter/channel/message/recall.html">Official documentation</a> */
    public ApiResponse deleteChannelsChannelIdMessagesMessageId(String channel_id, String message_id, ApiRequest request) {
        return call(ApiEndpoint.DELETE_CHANNELS_CHANNEL_ID_MESSAGES_MESSAGE_ID, Map.of("channel_id", channel_id, "message_id", message_id), request);
    }

    /** 表情表态. @see <a href="https://bot.q.qq.com/wiki/develop/api-v2/server-inter/message/trans/emoji.html">Official documentation</a> */
    public ApiResponse deleteChannelsChannelIdMessagesMessageIdReactionsTypeId(String channel_id, String message_id, String type, String id, ApiRequest request) {
        return call(ApiEndpoint.DELETE_CHANNELS_CHANNEL_ID_MESSAGES_MESSAGE_ID_REACTIONS_TYPE_ID, Map.of("channel_id", channel_id, "message_id", message_id, "type", type, "id", id), request);
    }

    /** 机器人下麦. @see <a href="https://bot.q.qq.com/wiki/develop/api-v2/server-inter/channel/content/audio/delete_mic.html">Official documentation</a> */
    public ApiResponse deleteChannelsChannelIdMic(String channel_id, ApiRequest request) {
        return call(ApiEndpoint.DELETE_CHANNELS_CHANNEL_ID_MIC, Map.of("channel_id", channel_id), request);
    }

    /** 删除精华消息. @see <a href="https://bot.q.qq.com/wiki/develop/api-v2/server-inter/channel/content/pins/delete_pins_message.html">Official documentation</a> */
    public ApiResponse deleteChannelsChannelIdPinsMessageId(String channel_id, String message_id, ApiRequest request) {
        return call(ApiEndpoint.DELETE_CHANNELS_CHANNEL_ID_PINS_MESSAGE_ID, Map.of("channel_id", channel_id, "message_id", message_id), request);
    }

    /** 删除日程. @see <a href="https://bot.q.qq.com/wiki/develop/api-v2/server-inter/channel/content/schedule/delete_schedule.html">Official documentation</a> */
    public ApiResponse deleteChannelsChannelIdSchedulesScheduleId(String channel_id, String schedule_id, ApiRequest request) {
        return call(ApiEndpoint.DELETE_CHANNELS_CHANNEL_ID_SCHEDULES_SCHEDULE_ID, Map.of("channel_id", channel_id, "schedule_id", schedule_id), request);
    }

    /** 删除帖子. @see <a href="https://bot.q.qq.com/wiki/develop/api-v2/server-inter/channel/content/forum/delete_thread.html">Official documentation</a> */
    public ApiResponse deleteChannelsChannelIdThreadsThreadId(String channel_id, String thread_id, ApiRequest request) {
        return call(ApiEndpoint.DELETE_CHANNELS_CHANNEL_ID_THREADS_THREAD_ID, Map.of("channel_id", channel_id, "thread_id", thread_id), request);
    }

    /** 频道私信. @see <a href="https://bot.q.qq.com/wiki/develop/api-v2/server-inter/channel/message/dms.html">Official documentation</a> */
    public ApiResponse deleteDmsGuildIdMessagesMessageId(String guild_id, String message_id, ApiRequest request) {
        return call(ApiEndpoint.DELETE_DMS_GUILD_ID_MESSAGES_MESSAGE_ID, Map.of("guild_id", guild_id, "message_id", message_id), request);
    }

    /** 删除频道公告. @see <a href="https://bot.q.qq.com/wiki/develop/api-v2/server-inter/channel/content/announces/delete_guild_announces.html">Official documentation</a> */
    public ApiResponse deleteGuildsGuildIdAnnouncesMessageId(String guild_id, String message_id, ApiRequest request) {
        return call(ApiEndpoint.DELETE_GUILDS_GUILD_ID_ANNOUNCES_MESSAGE_ID, Map.of("guild_id", guild_id, "message_id", message_id), request);
    }

    /** 删除频道成员. @see <a href="https://bot.q.qq.com/wiki/develop/api-v2/openapi/member/delete_member.html">Official documentation</a> */
    public ApiResponse deleteGuildsGuildIdMembersUserId(String guild_id, String user_id, ApiRequest request) {
        return call(ApiEndpoint.DELETE_GUILDS_GUILD_ID_MEMBERS_USER_ID, Map.of("guild_id", guild_id, "user_id", user_id), request);
    }

    /** 删除频道身份组成员. @see <a href="https://bot.q.qq.com/wiki/develop/api-v2/server-inter/channel/role-group/delete_guild_member_role.html">Official documentation</a> */
    public ApiResponse deleteGuildsGuildIdMembersUserIdRolesRoleId(String guild_id, String user_id, String role_id, ApiRequest request) {
        return call(ApiEndpoint.DELETE_GUILDS_GUILD_ID_MEMBERS_USER_ID_ROLES_ROLE_ID, Map.of("guild_id", guild_id, "user_id", user_id, "role_id", role_id), request);
    }

    /** 删除频道身份组. @see <a href="https://bot.q.qq.com/wiki/develop/api-v2/server-inter/channel/role-group/delete_guild_role.html">Official documentation</a> */
    public ApiResponse deleteGuildsGuildIdRolesRoleId(String guild_id, String role_id, ApiRequest request) {
        return call(ApiEndpoint.DELETE_GUILDS_GUILD_ID_ROLES_ROLE_ID, Map.of("guild_id", guild_id, "role_id", role_id), request);
    }

    /** 删除入群自动审批策略. @see <a href="https://bot.q.qq.com/wiki/develop/api-v2/autogen/api/v2_groups_join_approval_strategy_strategy_id.delete.html">Official documentation</a> */
    public ApiResponse deleteV2GroupsJoinApprovalStrategyStrategyId(String strategy_id, ApiRequest request) {
        return call(ApiEndpoint.DELETE_V2_GROUPS_JOIN_APPROVAL_STRATEGY_STRATEGY_ID, Map.of("strategy_id", strategy_id), request);
    }

    /** 撤回群聊消息. @see <a href="https://bot.q.qq.com/wiki/develop/api-v2/autogen/api/v2_groups_group_openid_messages_message_id.delete.html">Official documentation</a> */
    public ApiResponse deleteV2GroupsGroupOpenidMessagesMessageId(String group_openid, String message_id, ApiRequest request) {
        return call(ApiEndpoint.DELETE_V2_GROUPS_GROUP_OPENID_MESSAGES_MESSAGE_ID, Map.of("group_openid", group_openid, "message_id", message_id), request);
    }

    /** 素材管理. @see <a href="https://bot.q.qq.com/wiki/develop/api-v2/server-inter/message/material-management.html">Official documentation</a> */
    public ApiResponse deleteV2MediaImagesImageIdDelete(String image_id, ApiRequest request) {
        return call(ApiEndpoint.DELETE_V2_MEDIA_IMAGES_IMAGE_ID_DELETE, Map.of("image_id", image_id), request);
    }

    /** 删除指令面板. @see <a href="https://bot.q.qq.com/wiki/develop/api-v2/autogen/api/v2_panels_panel_id.delete.html">Official documentation</a> */
    public ApiResponse deleteV2PanelsPanelId(String panel_id, ApiRequest request) {
        return call(ApiEndpoint.DELETE_V2_PANELS_PANEL_ID, Map.of("panel_id", panel_id), request);
    }

    /** 撤回单聊消息. @see <a href="https://bot.q.qq.com/wiki/develop/api-v2/autogen/api/v2_users_user_openid_messages_message_id.delete.html">Official documentation</a> */
    public ApiResponse deleteV2UsersUserOpenidMessagesMessageId(String user_openid, String message_id, ApiRequest request) {
        return call(ApiEndpoint.DELETE_V2_USERS_USER_OPENID_MESSAGES_MESSAGE_ID, Map.of("user_openid", user_openid, "message_id", message_id), request);
    }

    /** 获取子频道详情. @see <a href="https://bot.q.qq.com/wiki/develop/api-v2/autogen/api/channels_channel_id.get.html">Official documentation</a> */
    public ApiResponse getChannelsChannelId(String channel_id, ApiRequest request) {
        return call(ApiEndpoint.GET_CHANNELS_CHANNEL_ID, Map.of("channel_id", channel_id), request);
    }

    /** 获取子频道用户权限. @see <a href="https://bot.q.qq.com/wiki/develop/api-v2/server-inter/channel/role-group/channel_permissions/get_channel_permissions.html">Official documentation</a> */
    public ApiResponse getChannelsChannelIdMembersUserIdPermissions(String channel_id, String user_id, ApiRequest request) {
        return call(ApiEndpoint.GET_CHANNELS_CHANNEL_ID_MEMBERS_USER_ID_PERMISSIONS, Map.of("channel_id", channel_id, "user_id", user_id), request);
    }

    /** 表情表态. @see <a href="https://bot.q.qq.com/wiki/develop/api-v2/server-inter/message/trans/emoji.html">Official documentation</a> */
    public ApiResponse getChannelsChannelIdMessagesMessageIdReactionsTypeId(String channel_id, String message_id, String type, String id, ApiRequest request) {
        return call(ApiEndpoint.GET_CHANNELS_CHANNEL_ID_MESSAGES_MESSAGE_ID_REACTIONS_TYPE_ID, Map.of("channel_id", channel_id, "message_id", message_id, "type", type, "id", id), request);
    }

    /** 获取子频道在线成员数. @see <a href="https://bot.q.qq.com/wiki/develop/api-v2/server-inter/channel/role/get_online_nums.html">Official documentation</a> */
    public ApiResponse getChannelsChannelIdOnlineNums(String channel_id, ApiRequest request) {
        return call(ApiEndpoint.GET_CHANNELS_CHANNEL_ID_ONLINE_NUMS, Map.of("channel_id", channel_id), request);
    }

    /** 获取精华消息. @see <a href="https://bot.q.qq.com/wiki/develop/api-v2/server-inter/channel/content/pins/get_pins_message.html">Official documentation</a> */
    public ApiResponse getChannelsChannelIdPins(String channel_id, ApiRequest request) {
        return call(ApiEndpoint.GET_CHANNELS_CHANNEL_ID_PINS, Map.of("channel_id", channel_id), request);
    }

    /** 获取子频道身份组权限. @see <a href="https://bot.q.qq.com/wiki/develop/api-v2/server-inter/channel/role-group/channel_permissions/get_channel_roles_permissions.html">Official documentation</a> */
    public ApiResponse getChannelsChannelIdRolesRoleIdPermissions(String channel_id, String role_id, ApiRequest request) {
        return call(ApiEndpoint.GET_CHANNELS_CHANNEL_ID_ROLES_ROLE_ID_PERMISSIONS, Map.of("channel_id", channel_id, "role_id", role_id), request);
    }

    /** 获取频道日程列表. @see <a href="https://bot.q.qq.com/wiki/develop/api-v2/server-inter/channel/content/schedule/get_schedules.html">Official documentation</a> */
    public ApiResponse getChannelsChannelIdSchedules(String channel_id, ApiRequest request) {
        return call(ApiEndpoint.GET_CHANNELS_CHANNEL_ID_SCHEDULES, Map.of("channel_id", channel_id), request);
    }

    /** 获取日程详情. @see <a href="https://bot.q.qq.com/wiki/develop/api-v2/server-inter/channel/content/schedule/get_schedule.html">Official documentation</a> */
    public ApiResponse getChannelsChannelIdSchedulesScheduleId(String channel_id, String schedule_id, ApiRequest request) {
        return call(ApiEndpoint.GET_CHANNELS_CHANNEL_ID_SCHEDULES_SCHEDULE_ID, Map.of("channel_id", channel_id, "schedule_id", schedule_id), request);
    }

    /** 获取帖子列表. @see <a href="https://bot.q.qq.com/wiki/develop/api-v2/server-inter/channel/content/forum/get_threads_list.html">Official documentation</a> */
    public ApiResponse getChannelsChannelIdThreads(String channel_id, ApiRequest request) {
        return call(ApiEndpoint.GET_CHANNELS_CHANNEL_ID_THREADS, Map.of("channel_id", channel_id), request);
    }

    /** 获取帖子详情. @see <a href="https://bot.q.qq.com/wiki/develop/api-v2/server-inter/channel/content/forum/get_thread.html">Official documentation</a> */
    public ApiResponse getChannelsChannelIdThreadsThreadId(String channel_id, String thread_id, ApiRequest request) {
        return call(ApiEndpoint.GET_CHANNELS_CHANNEL_ID_THREADS_THREAD_ID, Map.of("channel_id", channel_id, "thread_id", thread_id), request);
    }

    /** 获取通用 WSS 接入点. @see <a href="https://bot.q.qq.com/wiki/develop/api-v2/autogen/api/gateway.get.html">Official documentation</a> */
    public ApiResponse getGateway(ApiRequest request) {
        return call(ApiEndpoint.GET_GATEWAY, Map.of(), request);
    }

    /** 获取带分片 WSS 接入点. @see <a href="https://bot.q.qq.com/wiki/develop/api-v2/openapi/wss/shard_url_get.html">Official documentation</a> */
    public ApiResponse getGatewayBot(ApiRequest request) {
        return call(ApiEndpoint.GET_GATEWAY_BOT, Map.of(), request);
    }

    /** 获取频道详情. @see <a href="https://bot.q.qq.com/wiki/develop/api-v2/autogen/api/guilds_guild_id.get.html">Official documentation</a> */
    public ApiResponse getGuildsGuildId(String guild_id, ApiRequest request) {
        return call(ApiEndpoint.GET_GUILDS_GUILD_ID, Map.of("guild_id", guild_id), request);
    }

    /** 获取机器人在频道可用权限列表. @see <a href="https://bot.q.qq.com/wiki/develop/api-v2/server-inter/channel/api_permissions/get_guild_api_permission.html">Official documentation</a> */
    public ApiResponse getGuildsGuildIdApiPermission(String guild_id, ApiRequest request) {
        return call(ApiEndpoint.GET_GUILDS_GUILD_ID_API_PERMISSION, Map.of("guild_id", guild_id), request);
    }

    /** 获取子频道列表. @see <a href="https://bot.q.qq.com/wiki/develop/api-v2/autogen/api/guilds_guild_id_channels.get.html">Official documentation</a> */
    public ApiResponse getGuildsGuildIdChannels(String guild_id, ApiRequest request) {
        return call(ApiEndpoint.GET_GUILDS_GUILD_ID_CHANNELS, Map.of("guild_id", guild_id), request);
    }

    /** 获取频道成员列表. @see <a href="https://bot.q.qq.com/wiki/develop/api-v2/openapi/member/get_members.html">Official documentation</a> */
    public ApiResponse getGuildsGuildIdMembers(String guild_id, ApiRequest request) {
        return call(ApiEndpoint.GET_GUILDS_GUILD_ID_MEMBERS, Map.of("guild_id", guild_id), request);
    }

    /** 获取成员详情. @see <a href="https://bot.q.qq.com/wiki/develop/api-v2/openapi/member/get_member.html">Official documentation</a> */
    public ApiResponse getGuildsGuildIdMembersUserId(String guild_id, String user_id, ApiRequest request) {
        return call(ApiEndpoint.GET_GUILDS_GUILD_ID_MEMBERS_USER_ID, Map.of("guild_id", guild_id, "user_id", user_id), request);
    }

    /** 获取频道消息频率的设置详情. @see <a href="https://bot.q.qq.com/wiki/develop/api-v2/server-inter/channel/speak/setting/message_setting.html">Official documentation</a> */
    public ApiResponse getGuildsGuildIdMessageSetting(String guild_id, ApiRequest request) {
        return call(ApiEndpoint.GET_GUILDS_GUILD_ID_MESSAGE_SETTING, Map.of("guild_id", guild_id), request);
    }

    /** 获取频道身份组列表. @see <a href="https://bot.q.qq.com/wiki/develop/api-v2/server-inter/channel/role-group/get_guild_roles.html">Official documentation</a> */
    public ApiResponse getGuildsGuildIdRoles(String guild_id, ApiRequest request) {
        return call(ApiEndpoint.GET_GUILDS_GUILD_ID_ROLES, Map.of("guild_id", guild_id), request);
    }

    /** 获取频道身份组成员列表. @see <a href="https://bot.q.qq.com/wiki/develop/api-v2/server-inter/channel/role/member/get_role_members.html">Official documentation</a> */
    public ApiResponse getGuildsGuildIdRolesRoleIdMembers(String guild_id, String role_id, ApiRequest request) {
        return call(ApiEndpoint.GET_GUILDS_GUILD_ID_ROLES_ROLE_ID_MEMBERS, Map.of("guild_id", guild_id, "role_id", role_id), request);
    }

    /** 获取机器人详情. @see <a href="https://bot.q.qq.com/wiki/develop/api-v2/autogen/api/users_me.get.html">Official documentation</a> */
    public ApiResponse getUsersMe(ApiRequest request) {
        return call(ApiEndpoint.GET_USERS_ME, Map.of(), request);
    }

    /** 获取机器人频道列表. @see <a href="https://bot.q.qq.com/wiki/develop/api-v2/autogen/api/users_me_guilds.get.html">Official documentation</a> */
    public ApiResponse getUsersMeGuilds(ApiRequest request) {
        return call(ApiEndpoint.GET_USERS_ME_GUILDS, Map.of(), request);
    }

    /** 查询入群自动审批策略列表. @see <a href="https://bot.q.qq.com/wiki/develop/api-v2/autogen/api/v2_groups_join_approval_strategy.get.html">Official documentation</a> */
    public ApiResponse getV2GroupsJoinApprovalStrategy(ApiRequest request) {
        return call(ApiEndpoint.GET_V2_GROUPS_JOIN_APPROVAL_STRATEGY, Map.of(), request);
    }

    /** 获取机器人群内状态. @see <a href="https://bot.q.qq.com/wiki/develop/api-v2/autogen/api/v2_groups_group_openid_bot_state.get.html">Official documentation</a> */
    public ApiResponse getV2GroupsGroupOpenidBotState(String group_openid, ApiRequest request) {
        return call(ApiEndpoint.GET_V2_GROUPS_GROUP_OPENID_BOT_STATE, Map.of("group_openid", group_openid), request);
    }

    /** 获取群基本信息. @see <a href="https://bot.q.qq.com/wiki/develop/api-v2/autogen/api/v2_groups_group_openid_info.get.html">Official documentation</a> */
    public ApiResponse getV2GroupsGroupOpenidInfo(String group_openid, ApiRequest request) {
        return call(ApiEndpoint.GET_V2_GROUPS_GROUP_OPENID_INFO, Map.of("group_openid", group_openid), request);
    }

    /** 入群申请列表拉取. @see <a href="https://bot.q.qq.com/wiki/develop/api-v2/autogen/api/v2_groups_group_openid_join_request_list.get.html">Official documentation</a> */
    public ApiResponse getV2GroupsGroupOpenidJoinRequestList(String group_openid, ApiRequest request) {
        return call(ApiEndpoint.GET_V2_GROUPS_GROUP_OPENID_JOIN_REQUEST_LIST, Map.of("group_openid", group_openid), request);
    }

    /** 群黑名单查询. @see <a href="https://bot.q.qq.com/wiki/develop/api-v2/autogen/api/v2_groups_group_openid_member_blacklist.get.html">Official documentation</a> */
    public ApiResponse getV2GroupsGroupOpenidMemberBlacklist(String group_openid, ApiRequest request) {
        return call(ApiEndpoint.GET_V2_GROUPS_GROUP_OPENID_MEMBER_BLACKLIST, Map.of("group_openid", group_openid), request);
    }

    /** 获取群成员列表. @see <a href="https://bot.q.qq.com/wiki/develop/api-v2/autogen/api/v2_groups_group_openid_members.get.html">Official documentation</a> */
    public ApiResponse getV2GroupsGroupOpenidMembers(String group_openid, ApiRequest request) {
        return call(ApiEndpoint.GET_V2_GROUPS_GROUP_OPENID_MEMBERS, Map.of("group_openid", group_openid), request);
    }

    /** 获取群成员信息. @see <a href="https://bot.q.qq.com/wiki/develop/api-v2/autogen/api/v2_groups_group_openid_members_member_openid.get.html">Official documentation</a> */
    public ApiResponse getV2GroupsGroupOpenidMembersMemberOpenid(String group_openid, String member_openid, ApiRequest request) {
        return call(ApiEndpoint.GET_V2_GROUPS_GROUP_OPENID_MEMBERS_MEMBER_OPENID, Map.of("group_openid", group_openid, "member_openid", member_openid), request);
    }

    /** 查询群禁言状态. @see <a href="https://bot.q.qq.com/wiki/develop/api-v2/autogen/api/v2_groups_group_openid_restrict_chat_setting.get.html">Official documentation</a> */
    public ApiResponse getV2GroupsGroupOpenidRestrictChatSetting(String group_openid, ApiRequest request) {
        return call(ApiEndpoint.GET_V2_GROUPS_GROUP_OPENID_RESTRICT_CHAT_SETTING, Map.of("group_openid", group_openid), request);
    }

    /** 素材管理. @see <a href="https://bot.q.qq.com/wiki/develop/api-v2/server-inter/message/material-management.html">Official documentation</a> */
    public ApiResponse getV2MediaImages(ApiRequest request) {
        return call(ApiEndpoint.GET_V2_MEDIA_IMAGES, Map.of(), request);
    }

    /** 查询全局自定义菜单. @see <a href="https://bot.q.qq.com/wiki/develop/api-v2/autogen/api/v2_menu.get.html">Official documentation</a> */
    public ApiResponse getV2Menu(ApiRequest request) {
        return call(ApiEndpoint.GET_V2_MENU, Map.of(), request);
    }

    /** 查询指令面板列表. @see <a href="https://bot.q.qq.com/wiki/develop/api-v2/autogen/api/v2_panels.get.html">Official documentation</a> */
    public ApiResponse getV2Panels(ApiRequest request) {
        return call(ApiEndpoint.GET_V2_PANELS, Map.of(), request);
    }

    /** 查询指令面板详情. @see <a href="https://bot.q.qq.com/wiki/develop/api-v2/autogen/api/v2_panels_panel_id.get.html">Official documentation</a> */
    public ApiResponse getV2PanelsPanelId(String panel_id, ApiRequest request) {
        return call(ApiEndpoint.GET_V2_PANELS_PANEL_ID, Map.of("panel_id", panel_id), request);
    }

    /** 修改子频道. @see <a href="https://bot.q.qq.com/wiki/develop/api-v2/autogen/api/channels_channel_id.patch.html">Official documentation</a> */
    public ApiResponse patchChannelsChannelId(String channel_id, ApiRequest request) {
        return call(ApiEndpoint.PATCH_CHANNELS_CHANNEL_ID, Map.of("channel_id", channel_id), request);
    }

    /** 修改日程. @see <a href="https://bot.q.qq.com/wiki/develop/api-v2/server-inter/channel/content/schedule/patch_schedule.html">Official documentation</a> */
    public ApiResponse patchChannelsChannelIdSchedulesScheduleId(String channel_id, String schedule_id, ApiRequest request) {
        return call(ApiEndpoint.PATCH_CHANNELS_CHANNEL_ID_SCHEDULES_SCHEDULE_ID, Map.of("channel_id", channel_id, "schedule_id", schedule_id), request);
    }

    /** 频道指定成员禁言. @see <a href="https://bot.q.qq.com/wiki/develop/api-v2/server-inter/channel/speak/patch_guild_member_mute.html">Official documentation</a> */
    public ApiResponse patchGuildsGuildIdMembersUserIdMute(String guild_id, String user_id, ApiRequest request) {
        return call(ApiEndpoint.PATCH_GUILDS_GUILD_ID_MEMBERS_USER_ID_MUTE, Map.of("guild_id", guild_id, "user_id", user_id), request);
    }

    /** 频道全员禁言. @see <a href="https://bot.q.qq.com/wiki/develop/api-v2/server-inter/channel/speak/patch_guild_mute.html">Official documentation</a> */
    public ApiResponse patchGuildsGuildIdMute(String guild_id, ApiRequest request) {
        return call(ApiEndpoint.PATCH_GUILDS_GUILD_ID_MUTE, Map.of("guild_id", guild_id), request);
    }

    /** 修改频道身份组. @see <a href="https://bot.q.qq.com/wiki/develop/api-v2/server-inter/channel/role-group/patch_guild_role.html">Official documentation</a> */
    public ApiResponse patchGuildsGuildIdRolesRoleId(String guild_id, String role_id, ApiRequest request) {
        return call(ApiEndpoint.PATCH_GUILDS_GUILD_ID_ROLES_ROLE_ID, Map.of("guild_id", guild_id, "role_id", role_id), request);
    }

    /** 修改入群自动审批策略. @see <a href="https://bot.q.qq.com/wiki/develop/api-v2/autogen/api/v2_groups_join_approval_strategy_strategy_id.patch.html">Official documentation</a> */
    public ApiResponse patchV2GroupsJoinApprovalStrategyStrategyId(String strategy_id, ApiRequest request) {
        return call(ApiEndpoint.PATCH_V2_GROUPS_JOIN_APPROVAL_STRATEGY_STRATEGY_ID, Map.of("strategy_id", strategy_id), request);
    }

    /** 获取访问凭证. @see <a href="https://bot.q.qq.com/wiki/develop/api-v2/dev-prepare/access-token.html">Official documentation</a> */
    public ApiResponse postAppGetappaccesstoken(ApiRequest request) {
        return call(ApiEndpoint.POST_APP_GETAPPACCESSTOKEN, Map.of(), request);
    }

    /** 创建子频道公告（2022年3月15日后废弃）. @see <a href="https://bot.q.qq.com/wiki/develop/api-v2/server-inter/channel/content/announces/post_channel_announces.html">Official documentation</a> */
    public ApiResponse postChannelsChannelIdAnnounces(String channel_id, ApiRequest request) {
        return call(ApiEndpoint.POST_CHANNELS_CHANNEL_ID_ANNOUNCES, Map.of("channel_id", channel_id), request);
    }

    /** 音频控制. @see <a href="https://bot.q.qq.com/wiki/develop/api-v2/server-inter/channel/content/audio/audio_control.html">Official documentation</a> */
    public ApiResponse postChannelsChannelIdAudio(String channel_id, ApiRequest request) {
        return call(ApiEndpoint.POST_CHANNELS_CHANNEL_ID_AUDIO, Map.of("channel_id", channel_id), request);
    }

    /** 发送子频道消息. @see <a href="https://bot.q.qq.com/wiki/develop/api-v2/server-inter/channel/message/send.html">Official documentation</a> */
    public ApiResponse postChannelsChannelIdMessages(String channel_id, ApiRequest request) {
        return call(ApiEndpoint.POST_CHANNELS_CHANNEL_ID_MESSAGES, Map.of("channel_id", channel_id), request);
    }

    /** 创建日程. @see <a href="https://bot.q.qq.com/wiki/develop/api-v2/server-inter/channel/content/schedule/post_schedule.html">Official documentation</a> */
    public ApiResponse postChannelsChannelIdSchedules(String channel_id, ApiRequest request) {
        return call(ApiEndpoint.POST_CHANNELS_CHANNEL_ID_SCHEDULES, Map.of("channel_id", channel_id), request);
    }

    /** 频道私信. @see <a href="https://bot.q.qq.com/wiki/develop/api-v2/server-inter/channel/message/dms.html">Official documentation</a> */
    public ApiResponse postDmsGuildIdMessages(String guild_id, ApiRequest request) {
        return call(ApiEndpoint.POST_DMS_GUILD_ID_MESSAGES, Map.of("guild_id", guild_id), request);
    }

    /** 创建频道公告. @see <a href="https://bot.q.qq.com/wiki/develop/api-v2/server-inter/channel/content/announces/post_guild_announces.html">Official documentation</a> */
    public ApiResponse postGuildsGuildIdAnnounces(String guild_id, ApiRequest request) {
        return call(ApiEndpoint.POST_GUILDS_GUILD_ID_ANNOUNCES, Map.of("guild_id", guild_id), request);
    }

    /** 发送机器人在频道接口权限的授权链接. @see <a href="https://bot.q.qq.com/wiki/develop/api-v2/server-inter/channel/api_permissions/post_api_permission_demand.html">Official documentation</a> */
    public ApiResponse postGuildsGuildIdApiPermissionDemand(String guild_id, ApiRequest request) {
        return call(ApiEndpoint.POST_GUILDS_GUILD_ID_API_PERMISSION_DEMAND, Map.of("guild_id", guild_id), request);
    }

    /** 创建子频道. @see <a href="https://bot.q.qq.com/wiki/develop/api-v2/autogen/api/guilds_guild_id_channels.post.html">Official documentation</a> */
    public ApiResponse postGuildsGuildIdChannels(String guild_id, ApiRequest request) {
        return call(ApiEndpoint.POST_GUILDS_GUILD_ID_CHANNELS, Map.of("guild_id", guild_id), request);
    }

    /** 创建频道身份组. @see <a href="https://bot.q.qq.com/wiki/develop/api-v2/server-inter/channel/role-group/post_guild_role.html">Official documentation</a> */
    public ApiResponse postGuildsGuildIdRoles(String guild_id, ApiRequest request) {
        return call(ApiEndpoint.POST_GUILDS_GUILD_ID_ROLES, Map.of("guild_id", guild_id), request);
    }

    /** 频道私信. @see <a href="https://bot.q.qq.com/wiki/develop/api-v2/server-inter/channel/message/dms.html">Official documentation</a> */
    public ApiResponse postUsersMeDms(ApiRequest request) {
        return call(ApiEndpoint.POST_USERS_ME_DMS, Map.of(), request);
    }

    /** 生成分享链接. @see <a href="https://bot.q.qq.com/wiki/develop/api-v2/autogen/api/v2_generate_url_link.post.html">Official documentation</a> */
    public ApiResponse postV2GenerateUrlLink(ApiRequest request) {
        return call(ApiEndpoint.POST_V2_GENERATE_URL_LINK, Map.of(), request);
    }

    /** 创建入群自动审批策略. @see <a href="https://bot.q.qq.com/wiki/develop/api-v2/autogen/api/v2_groups_join_approval_strategy.post.html">Official documentation</a> */
    public ApiResponse postV2GroupsJoinApprovalStrategy(ApiRequest request) {
        return call(ApiEndpoint.POST_V2_GROUPS_JOIN_APPROVAL_STRATEGY, Map.of(), request);
    }

    /** 执行入群自动审批策略. @see <a href="https://bot.q.qq.com/wiki/develop/api-v2/autogen/api/v2_groups_join_approval_strategy_strategy_id_execute.post.html">Official documentation</a> */
    public ApiResponse postV2GroupsJoinApprovalStrategyStrategyIdExecute(String strategy_id, ApiRequest request) {
        return call(ApiEndpoint.POST_V2_GROUPS_JOIN_APPROVAL_STRATEGY_STRATEGY_ID_EXECUTE, Map.of("strategy_id", strategy_id), request);
    }

    /** 修改入群自动审批策略的白名单号码. @see <a href="https://bot.q.qq.com/wiki/develop/api-v2/autogen/api/v2_groups_join_approval_strategy_strategy_id_whitelist_users.post.html">Official documentation</a> */
    public ApiResponse postV2GroupsJoinApprovalStrategyStrategyIdWhitelistUsers(String strategy_id, ApiRequest request) {
        return call(ApiEndpoint.POST_V2_GROUPS_JOIN_APPROVAL_STRATEGY_STRATEGY_ID_WHITELIST_USERS, Map.of("strategy_id", strategy_id), request);
    }

    /** 群聊分片上传完成. @see <a href="https://bot.q.qq.com/wiki/develop/api-v2/autogen/api/v2_groups_group_id_upload_part_finish.post.html">Official documentation</a> */
    public ApiResponse postV2GroupsGroupIdUploadPartFinish(String group_id, ApiRequest request) {
        return call(ApiEndpoint.POST_V2_GROUPS_GROUP_ID_UPLOAD_PART_FINISH, Map.of("group_id", group_id), request);
    }

    /** 群聊富媒体预上传. @see <a href="https://bot.q.qq.com/wiki/develop/api-v2/autogen/api/v2_groups_group_id_upload_prepare.post.html">Official documentation</a> */
    public ApiResponse postV2GroupsGroupIdUploadPrepare(String group_id, ApiRequest request) {
        return call(ApiEndpoint.POST_V2_GROUPS_GROUP_ID_UPLOAD_PREPARE, Map.of("group_id", group_id), request);
    }

    /** 入群申请审批. @see <a href="https://bot.q.qq.com/wiki/develop/api-v2/autogen/api/v2_groups_group_openid_approval_join_request_member_openid.post.html">Official documentation</a> */
    public ApiResponse postV2GroupsGroupOpenidApprovalJoinRequestMemberOpenid(String group_openid, String member_openid, ApiRequest request) {
        return call(ApiEndpoint.POST_V2_GROUPS_GROUP_OPENID_APPROVAL_JOIN_REQUEST_MEMBER_OPENID, Map.of("group_openid", group_openid, "member_openid", member_openid), request);
    }

    /** 群成员批量移除. @see <a href="https://bot.q.qq.com/wiki/develop/api-v2/autogen/api/v2_groups_group_openid_batch_remove_members.post.html">Official documentation</a> */
    public ApiResponse postV2GroupsGroupOpenidBatchRemoveMembers(String group_openid, ApiRequest request) {
        return call(ApiEndpoint.POST_V2_GROUPS_GROUP_OPENID_BATCH_REMOVE_MEMBERS, Map.of("group_openid", group_openid), request);
    }

    /** 群聊富媒体上传. @see <a href="https://bot.q.qq.com/wiki/develop/api-v2/autogen/api/v2_groups_group_openid_files.post.html">Official documentation</a> */
    public ApiResponse postV2GroupsGroupOpenidFiles(String group_openid, ApiRequest request) {
        return call(ApiEndpoint.POST_V2_GROUPS_GROUP_OPENID_FILES, Map.of("group_openid", group_openid), request);
    }

    /** 群黑名单操作. @see <a href="https://bot.q.qq.com/wiki/develop/api-v2/autogen/api/v2_groups_group_openid_member_blacklist.post.html">Official documentation</a> */
    public ApiResponse postV2GroupsGroupOpenidMemberBlacklist(String group_openid, ApiRequest request) {
        return call(ApiEndpoint.POST_V2_GROUPS_GROUP_OPENID_MEMBER_BLACKLIST, Map.of("group_openid", group_openid), request);
    }

    /** 发送群聊消息. @see <a href="https://bot.q.qq.com/wiki/develop/api-v2/autogen/api/v2_groups_group_openid_messages.post.html">Official documentation</a> */
    public ApiResponse postV2GroupsGroupOpenidMessages(String group_openid, ApiRequest request) {
        return call(ApiEndpoint.POST_V2_GROUPS_GROUP_OPENID_MESSAGES, Map.of("group_openid", group_openid), request);
    }

    /** 设置群成员禁言. @see <a href="https://bot.q.qq.com/wiki/develop/api-v2/autogen/api/v2_groups_group_openid_restrict_chat_setting.post.html">Official documentation</a> */
    public ApiResponse postV2GroupsGroupOpenidRestrictChatSetting(String group_openid, ApiRequest request) {
        return call(ApiEndpoint.POST_V2_GROUPS_GROUP_OPENID_RESTRICT_CHAT_SETTING, Map.of("group_openid", group_openid), request);
    }

    /** 素材管理. @see <a href="https://bot.q.qq.com/wiki/develop/api-v2/server-inter/message/material-management.html">Official documentation</a> */
    public ApiResponse postV2MediaImagesComplete(ApiRequest request) {
        return call(ApiEndpoint.POST_V2_MEDIA_IMAGES_COMPLETE, Map.of(), request);
    }

    /** 素材管理. @see <a href="https://bot.q.qq.com/wiki/develop/api-v2/server-inter/message/material-management.html">Official documentation</a> */
    public ApiResponse postV2MediaImagesPrepare(ApiRequest request) {
        return call(ApiEndpoint.POST_V2_MEDIA_IMAGES_PREPARE, Map.of(), request);
    }

    /** 创建指令面板. @see <a href="https://bot.q.qq.com/wiki/develop/api-v2/autogen/api/v2_panels.post.html">Official documentation</a> */
    public ApiResponse postV2Panels(ApiRequest request) {
        return call(ApiEndpoint.POST_V2_PANELS, Map.of(), request);
    }

    /** 单聊分片上传完成. @see <a href="https://bot.q.qq.com/wiki/develop/api-v2/autogen/api/v2_users_user_id_upload_part_finish.post.html">Official documentation</a> */
    public ApiResponse postV2UsersUserIdUploadPartFinish(String user_id, ApiRequest request) {
        return call(ApiEndpoint.POST_V2_USERS_USER_ID_UPLOAD_PART_FINISH, Map.of("user_id", user_id), request);
    }

    /** 单聊富媒体预上传. @see <a href="https://bot.q.qq.com/wiki/develop/api-v2/autogen/api/v2_users_user_id_upload_prepare.post.html">Official documentation</a> */
    public ApiResponse postV2UsersUserIdUploadPrepare(String user_id, ApiRequest request) {
        return call(ApiEndpoint.POST_V2_USERS_USER_ID_UPLOAD_PREPARE, Map.of("user_id", user_id), request);
    }

    /** 单聊富媒体上传. @see <a href="https://bot.q.qq.com/wiki/develop/api-v2/autogen/api/v2_users_user_openid_files.post.html">Official documentation</a> */
    public ApiResponse postV2UsersUserOpenidFiles(String user_openid, ApiRequest request) {
        return call(ApiEndpoint.POST_V2_USERS_USER_OPENID_FILES, Map.of("user_openid", user_openid), request);
    }

    /** 发送单聊消息. @see <a href="https://bot.q.qq.com/wiki/develop/api-v2/autogen/api/v2_users_user_openid_messages.post.html">Official documentation</a> */
    public ApiResponse postV2UsersUserOpenidMessages(String user_openid, ApiRequest request) {
        return call(ApiEndpoint.POST_V2_USERS_USER_OPENID_MESSAGES, Map.of("user_openid", user_openid), request);
    }

    /** 流式发送单聊消息. @see <a href="https://bot.q.qq.com/wiki/develop/api-v2/autogen/api/v2_users_user_openid_stream_messages.post.html">Official documentation</a> */
    public ApiResponse postV2UsersUserOpenidStreamMessages(String user_openid, ApiRequest request) {
        return call(ApiEndpoint.POST_V2_USERS_USER_OPENID_STREAM_MESSAGES, Map.of("user_openid", user_openid), request);
    }

    /** 修改子频道用户权限. @see <a href="https://bot.q.qq.com/wiki/develop/api-v2/server-inter/channel/role-group/channel_permissions/put_channel_permissions.html">Official documentation</a> */
    public ApiResponse putChannelsChannelIdMembersUserIdPermissions(String channel_id, String user_id, ApiRequest request) {
        return call(ApiEndpoint.PUT_CHANNELS_CHANNEL_ID_MEMBERS_USER_ID_PERMISSIONS, Map.of("channel_id", channel_id, "user_id", user_id), request);
    }

    /** 表情表态. @see <a href="https://bot.q.qq.com/wiki/develop/api-v2/server-inter/message/trans/emoji.html">Official documentation</a> */
    public ApiResponse putChannelsChannelIdMessagesMessageIdReactionsTypeId(String channel_id, String message_id, String type, String id, ApiRequest request) {
        return call(ApiEndpoint.PUT_CHANNELS_CHANNEL_ID_MESSAGES_MESSAGE_ID_REACTIONS_TYPE_ID, Map.of("channel_id", channel_id, "message_id", message_id, "type", type, "id", id), request);
    }

    /** 机器人上麦. @see <a href="https://bot.q.qq.com/wiki/develop/api-v2/server-inter/channel/content/audio/put_mic.html">Official documentation</a> */
    public ApiResponse putChannelsChannelIdMic(String channel_id, ApiRequest request) {
        return call(ApiEndpoint.PUT_CHANNELS_CHANNEL_ID_MIC, Map.of("channel_id", channel_id), request);
    }

    /** 添加精华消息. @see <a href="https://bot.q.qq.com/wiki/develop/api-v2/server-inter/channel/content/pins/put_pins_message.html">Official documentation</a> */
    public ApiResponse putChannelsChannelIdPinsMessageId(String channel_id, String message_id, ApiRequest request) {
        return call(ApiEndpoint.PUT_CHANNELS_CHANNEL_ID_PINS_MESSAGE_ID, Map.of("channel_id", channel_id, "message_id", message_id), request);
    }

    /** 修改子频道身份组权限. @see <a href="https://bot.q.qq.com/wiki/develop/api-v2/server-inter/channel/role-group/channel_permissions/put_channel_roles_permissions.html">Official documentation</a> */
    public ApiResponse putChannelsChannelIdRolesRoleIdPermissions(String channel_id, String role_id, ApiRequest request) {
        return call(ApiEndpoint.PUT_CHANNELS_CHANNEL_ID_ROLES_ROLE_ID_PERMISSIONS, Map.of("channel_id", channel_id, "role_id", role_id), request);
    }

    /** 发表帖子. @see <a href="https://bot.q.qq.com/wiki/develop/api-v2/server-inter/channel/content/forum/put_thread.html">Official documentation</a> */
    public ApiResponse putChannelsChannelIdThreads(String channel_id, ApiRequest request) {
        return call(ApiEndpoint.PUT_CHANNELS_CHANNEL_ID_THREADS, Map.of("channel_id", channel_id), request);
    }

    /** 创建频道身份组成员. @see <a href="https://bot.q.qq.com/wiki/develop/api-v2/server-inter/channel/role-group/put_guild_member_role.html">Official documentation</a> */
    public ApiResponse putGuildsGuildIdMembersUserIdRolesRoleId(String guild_id, String user_id, String role_id, ApiRequest request) {
        return call(ApiEndpoint.PUT_GUILDS_GUILD_ID_MEMBERS_USER_ID_ROLES_ROLE_ID, Map.of("guild_id", guild_id, "user_id", user_id, "role_id", role_id), request);
    }

    /** 互动事件响应. @see <a href="https://bot.q.qq.com/wiki/develop/api-v2/autogen/api/interactions_interaction_id.put.html">Official documentation</a> */
    public ApiResponse putInteractionsInteractionId(String interaction_id, ApiRequest request) {
        return call(ApiEndpoint.PUT_INTERACTIONS_INTERACTION_ID, Map.of("interaction_id", interaction_id), request);
    }

    /** 修改全局自定义菜单. @see <a href="https://bot.q.qq.com/wiki/develop/api-v2/autogen/api/v2_menu.put.html">Official documentation</a> */
    public ApiResponse putV2Menu(ApiRequest request) {
        return call(ApiEndpoint.PUT_V2_MENU, Map.of(), request);
    }

    /** 修改指令面板. @see <a href="https://bot.q.qq.com/wiki/develop/api-v2/autogen/api/v2_panels_panel_id.put.html">Official documentation</a> */
    public ApiResponse putV2PanelsPanelId(String panel_id, ApiRequest request) {
        return call(ApiEndpoint.PUT_V2_PANELS_PANEL_ID, Map.of("panel_id", panel_id), request);
    }

    /** 修改指令面板关联对象. @see <a href="https://bot.q.qq.com/wiki/develop/api-v2/autogen/api/v2_panels_panel_id_target.put.html">Official documentation</a> */
    public ApiResponse putV2PanelsPanelIdTarget(String panel_id, ApiRequest request) {
        return call(ApiEndpoint.PUT_V2_PANELS_PANEL_ID_TARGET, Map.of("panel_id", panel_id), request);
    }
}
