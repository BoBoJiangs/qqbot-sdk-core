# QQ Java SDK 接口覆盖清单

快照日期：2026-11-05；扫描 192 个官方文档页面，收录 100 个 REST 接口和 49 种事件。

由 `scripts/sync_qq_sdk_catalog.py` 生成；端点、来源与页面摘要见 `spec/official-api.json`。

所有 REST 接口均有独立 Java 方法，公共字段通过 ApiRequest 的 JSON Map、query、multipart 或原始字节传入。
标记内邀的接口已封装，实际访问权限由 QQ 平台决定。

| 方法 | 路径 | Java 方法 | 官方文档 |
|---|---|---|---|
| DELETE | `/channels/{channel_id}` | `deleteChannelsChannelId` | [删除子频道](https://bot.q.qq.com/wiki/develop/api-v2/autogen/api/channels_channel_id.delete.html) |
| DELETE | `/channels/{channel_id}/announces/{message_id}` | `deleteChannelsChannelIdAnnouncesMessageId` | [删除子频道公告（2022年3月15日后废弃）](https://bot.q.qq.com/wiki/develop/api-v2/server-inter/channel/content/announces/delete_channel_announces.html) |
| DELETE | `/channels/{channel_id}/messages/{message_id}` | `deleteChannelsChannelIdMessagesMessageId` | [撤回子频道消息](https://bot.q.qq.com/wiki/develop/api-v2/server-inter/channel/message/recall.html) |
| DELETE | `/channels/{channel_id}/messages/{message_id}/reactions/{type}/{id}` | `deleteChannelsChannelIdMessagesMessageIdReactionsTypeId` | [表情表态](https://bot.q.qq.com/wiki/develop/api-v2/server-inter/message/trans/emoji.html) |
| DELETE | `/channels/{channel_id}/mic` | `deleteChannelsChannelIdMic` | [机器人下麦](https://bot.q.qq.com/wiki/develop/api-v2/server-inter/channel/content/audio/delete_mic.html) |
| DELETE | `/channels/{channel_id}/pins/{message_id}` | `deleteChannelsChannelIdPinsMessageId` | [删除精华消息](https://bot.q.qq.com/wiki/develop/api-v2/server-inter/channel/content/pins/delete_pins_message.html) |
| DELETE | `/channels/{channel_id}/schedules/{schedule_id}` | `deleteChannelsChannelIdSchedulesScheduleId` | [删除日程](https://bot.q.qq.com/wiki/develop/api-v2/server-inter/channel/content/schedule/delete_schedule.html) |
| DELETE | `/channels/{channel_id}/threads/{thread_id}` | `deleteChannelsChannelIdThreadsThreadId` | [删除帖子](https://bot.q.qq.com/wiki/develop/api-v2/server-inter/channel/content/forum/delete_thread.html) |
| DELETE | `/dms/{guild_id}/messages/{message_id}` | `deleteDmsGuildIdMessagesMessageId` | [频道私信](https://bot.q.qq.com/wiki/develop/api-v2/server-inter/channel/message/dms.html) |
| DELETE | `/guilds/{guild_id}/announces/{message_id}` | `deleteGuildsGuildIdAnnouncesMessageId` | [删除频道公告](https://bot.q.qq.com/wiki/develop/api-v2/server-inter/channel/content/announces/delete_guild_announces.html) |
| DELETE | `/guilds/{guild_id}/members/{user_id}` | `deleteGuildsGuildIdMembersUserId` | [删除频道成员](https://bot.q.qq.com/wiki/develop/api-v2/openapi/member/delete_member.html) |
| DELETE | `/guilds/{guild_id}/members/{user_id}/roles/{role_id}` | `deleteGuildsGuildIdMembersUserIdRolesRoleId` | [删除频道身份组成员](https://bot.q.qq.com/wiki/develop/api-v2/server-inter/channel/role-group/delete_guild_member_role.html) |
| DELETE | `/guilds/{guild_id}/roles/{role_id}` | `deleteGuildsGuildIdRolesRoleId` | [删除频道身份组](https://bot.q.qq.com/wiki/develop/api-v2/server-inter/channel/role-group/delete_guild_role.html) |
| DELETE | `/v2/groups/join_approval_strategy/{strategy_id}` | `deleteV2GroupsJoinApprovalStrategyStrategyId` | [删除入群自动审批策略](https://bot.q.qq.com/wiki/develop/api-v2/autogen/api/v2_groups_join_approval_strategy_strategy_id.delete.html) |
| DELETE | `/v2/groups/{group_openid}/messages/{message_id}` | `deleteV2GroupsGroupOpenidMessagesMessageId` | [撤回群聊消息](https://bot.q.qq.com/wiki/develop/api-v2/autogen/api/v2_groups_group_openid_messages_message_id.delete.html) |
| DELETE | `/v2/media-images/{image_id}/delete` | `deleteV2MediaImagesImageIdDelete` | [素材管理](https://bot.q.qq.com/wiki/develop/api-v2/server-inter/message/material-management.html) |
| DELETE | `/v2/panels/{panel_id}` | `deleteV2PanelsPanelId` | [删除指令面板](https://bot.q.qq.com/wiki/develop/api-v2/autogen/api/v2_panels_panel_id.delete.html) |
| DELETE | `/v2/users/{user_openid}/messages/{message_id}` | `deleteV2UsersUserOpenidMessagesMessageId` | [撤回单聊消息](https://bot.q.qq.com/wiki/develop/api-v2/autogen/api/v2_users_user_openid_messages_message_id.delete.html) |
| GET | `/channels/{channel_id}` | `getChannelsChannelId` | [获取子频道详情](https://bot.q.qq.com/wiki/develop/api-v2/autogen/api/channels_channel_id.get.html) |
| GET | `/channels/{channel_id}/members/{user_id}/permissions` | `getChannelsChannelIdMembersUserIdPermissions` | [获取子频道用户权限](https://bot.q.qq.com/wiki/develop/api-v2/server-inter/channel/role-group/channel_permissions/get_channel_permissions.html) |
| GET | `/channels/{channel_id}/messages/{message_id}/reactions/{type}/{id}` | `getChannelsChannelIdMessagesMessageIdReactionsTypeId` | [表情表态](https://bot.q.qq.com/wiki/develop/api-v2/server-inter/message/trans/emoji.html) |
| GET | `/channels/{channel_id}/online_nums` | `getChannelsChannelIdOnlineNums` | [获取子频道在线成员数](https://bot.q.qq.com/wiki/develop/api-v2/server-inter/channel/role/get_online_nums.html) |
| GET | `/channels/{channel_id}/pins` | `getChannelsChannelIdPins` | [获取精华消息](https://bot.q.qq.com/wiki/develop/api-v2/server-inter/channel/content/pins/get_pins_message.html) |
| GET | `/channels/{channel_id}/roles/{role_id}/permissions` | `getChannelsChannelIdRolesRoleIdPermissions` | [获取子频道身份组权限](https://bot.q.qq.com/wiki/develop/api-v2/server-inter/channel/role-group/channel_permissions/get_channel_roles_permissions.html) |
| GET | `/channels/{channel_id}/schedules` | `getChannelsChannelIdSchedules` | [获取频道日程列表](https://bot.q.qq.com/wiki/develop/api-v2/server-inter/channel/content/schedule/get_schedules.html) |
| GET | `/channels/{channel_id}/schedules/{schedule_id}` | `getChannelsChannelIdSchedulesScheduleId` | [获取日程详情](https://bot.q.qq.com/wiki/develop/api-v2/server-inter/channel/content/schedule/get_schedule.html) |
| GET | `/channels/{channel_id}/threads` | `getChannelsChannelIdThreads` | [获取帖子列表](https://bot.q.qq.com/wiki/develop/api-v2/server-inter/channel/content/forum/get_threads_list.html) |
| GET | `/channels/{channel_id}/threads/{thread_id}` | `getChannelsChannelIdThreadsThreadId` | [获取帖子详情](https://bot.q.qq.com/wiki/develop/api-v2/server-inter/channel/content/forum/get_thread.html) |
| GET | `/gateway` | `getGateway` | [获取通用 WSS 接入点](https://bot.q.qq.com/wiki/develop/api-v2/autogen/api/gateway.get.html) |
| GET | `/gateway/bot` | `getGatewayBot` | [获取带分片 WSS 接入点](https://bot.q.qq.com/wiki/develop/api-v2/openapi/wss/shard_url_get.html) |
| GET | `/guilds/{guild_id}` | `getGuildsGuildId` | [获取频道详情](https://bot.q.qq.com/wiki/develop/api-v2/autogen/api/guilds_guild_id.get.html) |
| GET | `/guilds/{guild_id}/api_permission` | `getGuildsGuildIdApiPermission` | [获取机器人在频道可用权限列表](https://bot.q.qq.com/wiki/develop/api-v2/server-inter/channel/api_permissions/get_guild_api_permission.html) |
| GET | `/guilds/{guild_id}/channels` | `getGuildsGuildIdChannels` | [获取子频道列表](https://bot.q.qq.com/wiki/develop/api-v2/autogen/api/guilds_guild_id_channels.get.html) |
| GET | `/guilds/{guild_id}/members` | `getGuildsGuildIdMembers` | [获取频道成员列表](https://bot.q.qq.com/wiki/develop/api-v2/openapi/member/get_members.html) |
| GET | `/guilds/{guild_id}/members/{user_id}` | `getGuildsGuildIdMembersUserId` | [获取成员详情](https://bot.q.qq.com/wiki/develop/api-v2/openapi/member/get_member.html) |
| GET | `/guilds/{guild_id}/message/setting` | `getGuildsGuildIdMessageSetting` | [获取频道消息频率的设置详情](https://bot.q.qq.com/wiki/develop/api-v2/server-inter/channel/speak/setting/message_setting.html) |
| GET | `/guilds/{guild_id}/roles` | `getGuildsGuildIdRoles` | [获取频道身份组列表](https://bot.q.qq.com/wiki/develop/api-v2/server-inter/channel/role-group/get_guild_roles.html) |
| GET | `/guilds/{guild_id}/roles/{role_id}/members` | `getGuildsGuildIdRolesRoleIdMembers` | [获取频道身份组成员列表](https://bot.q.qq.com/wiki/develop/api-v2/server-inter/channel/role/member/get_role_members.html) |
| GET | `/users/@me` | `getUsersMe` | [获取机器人详情](https://bot.q.qq.com/wiki/develop/api-v2/autogen/api/users_me.get.html) |
| GET | `/users/@me/guilds` | `getUsersMeGuilds` | [获取机器人频道列表](https://bot.q.qq.com/wiki/develop/api-v2/autogen/api/users_me_guilds.get.html) |
| GET | `/v2/groups/join_approval_strategy` | `getV2GroupsJoinApprovalStrategy` | [查询入群自动审批策略列表](https://bot.q.qq.com/wiki/develop/api-v2/autogen/api/v2_groups_join_approval_strategy.get.html) |
| GET | `/v2/groups/{group_openid}/bot_state` | `getV2GroupsGroupOpenidBotState` | [获取机器人群内状态](https://bot.q.qq.com/wiki/develop/api-v2/autogen/api/v2_groups_group_openid_bot_state.get.html) |
| GET | `/v2/groups/{group_openid}/info` | `getV2GroupsGroupOpenidInfo` | [获取群基本信息](https://bot.q.qq.com/wiki/develop/api-v2/autogen/api/v2_groups_group_openid_info.get.html) |
| GET | `/v2/groups/{group_openid}/join_request_list` | `getV2GroupsGroupOpenidJoinRequestList` | [入群申请列表拉取](https://bot.q.qq.com/wiki/develop/api-v2/autogen/api/v2_groups_group_openid_join_request_list.get.html) |
| GET | `/v2/groups/{group_openid}/member_blacklist` | `getV2GroupsGroupOpenidMemberBlacklist` | [群黑名单查询](https://bot.q.qq.com/wiki/develop/api-v2/autogen/api/v2_groups_group_openid_member_blacklist.get.html) |
| GET | `/v2/groups/{group_openid}/members` | `getV2GroupsGroupOpenidMembers` | [获取群成员列表](https://bot.q.qq.com/wiki/develop/api-v2/autogen/api/v2_groups_group_openid_members.get.html) |
| GET | `/v2/groups/{group_openid}/members/{member_openid}` | `getV2GroupsGroupOpenidMembersMemberOpenid` | [获取群成员信息](https://bot.q.qq.com/wiki/develop/api-v2/autogen/api/v2_groups_group_openid_members_member_openid.get.html) |
| GET | `/v2/groups/{group_openid}/restrict_chat_setting` | `getV2GroupsGroupOpenidRestrictChatSetting` | [查询群禁言状态](https://bot.q.qq.com/wiki/develop/api-v2/autogen/api/v2_groups_group_openid_restrict_chat_setting.get.html) |
| GET | `/v2/media-images` | `getV2MediaImages` | [素材管理](https://bot.q.qq.com/wiki/develop/api-v2/server-inter/message/material-management.html) |
| GET | `/v2/menu` | `getV2Menu` | [查询全局自定义菜单](https://bot.q.qq.com/wiki/develop/api-v2/autogen/api/v2_menu.get.html) |
| GET | `/v2/panels` | `getV2Panels` | [查询指令面板列表](https://bot.q.qq.com/wiki/develop/api-v2/autogen/api/v2_panels.get.html) |
| GET | `/v2/panels/{panel_id}` | `getV2PanelsPanelId` | [查询指令面板详情](https://bot.q.qq.com/wiki/develop/api-v2/autogen/api/v2_panels_panel_id.get.html) |
| PATCH | `/channels/{channel_id}` | `patchChannelsChannelId` | [修改子频道](https://bot.q.qq.com/wiki/develop/api-v2/autogen/api/channels_channel_id.patch.html) |
| PATCH | `/channels/{channel_id}/schedules/{schedule_id}` | `patchChannelsChannelIdSchedulesScheduleId` | [修改日程](https://bot.q.qq.com/wiki/develop/api-v2/server-inter/channel/content/schedule/patch_schedule.html) |
| PATCH | `/guilds/{guild_id}/members/{user_id}/mute` | `patchGuildsGuildIdMembersUserIdMute` | [频道指定成员禁言](https://bot.q.qq.com/wiki/develop/api-v2/server-inter/channel/speak/patch_guild_member_mute.html) |
| PATCH | `/guilds/{guild_id}/mute` | `patchGuildsGuildIdMute` | [频道全员禁言](https://bot.q.qq.com/wiki/develop/api-v2/server-inter/channel/speak/patch_guild_mute.html) |
| PATCH | `/guilds/{guild_id}/roles/{role_id}` | `patchGuildsGuildIdRolesRoleId` | [修改频道身份组](https://bot.q.qq.com/wiki/develop/api-v2/server-inter/channel/role-group/patch_guild_role.html) |
| PATCH | `/v2/groups/join_approval_strategy/{strategy_id}` | `patchV2GroupsJoinApprovalStrategyStrategyId` | [修改入群自动审批策略](https://bot.q.qq.com/wiki/develop/api-v2/autogen/api/v2_groups_join_approval_strategy_strategy_id.patch.html) |
| POST | `/app/getAppAccessToken` | `postAppGetappaccesstoken` | [获取访问凭证](https://bot.q.qq.com/wiki/develop/api-v2/dev-prepare/access-token.html) |
| POST | `/channels/{channel_id}/announces` | `postChannelsChannelIdAnnounces` | [创建子频道公告（2022年3月15日后废弃）](https://bot.q.qq.com/wiki/develop/api-v2/server-inter/channel/content/announces/post_channel_announces.html) |
| POST | `/channels/{channel_id}/audio` | `postChannelsChannelIdAudio` | [音频控制](https://bot.q.qq.com/wiki/develop/api-v2/server-inter/channel/content/audio/audio_control.html) |
| POST | `/channels/{channel_id}/messages` | `postChannelsChannelIdMessages` | [发送子频道消息](https://bot.q.qq.com/wiki/develop/api-v2/server-inter/channel/message/send.html) |
| POST | `/channels/{channel_id}/schedules` | `postChannelsChannelIdSchedules` | [创建日程](https://bot.q.qq.com/wiki/develop/api-v2/server-inter/channel/content/schedule/post_schedule.html) |
| POST | `/dms/{guild_id}/messages` | `postDmsGuildIdMessages` | [频道私信](https://bot.q.qq.com/wiki/develop/api-v2/server-inter/channel/message/dms.html) |
| POST | `/guilds/{guild_id}/announces` | `postGuildsGuildIdAnnounces` | [创建频道公告](https://bot.q.qq.com/wiki/develop/api-v2/server-inter/channel/content/announces/post_guild_announces.html) |
| POST | `/guilds/{guild_id}/api_permission/demand` | `postGuildsGuildIdApiPermissionDemand` | [发送机器人在频道接口权限的授权链接](https://bot.q.qq.com/wiki/develop/api-v2/server-inter/channel/api_permissions/post_api_permission_demand.html) |
| POST | `/guilds/{guild_id}/channels` | `postGuildsGuildIdChannels` | [创建子频道](https://bot.q.qq.com/wiki/develop/api-v2/autogen/api/guilds_guild_id_channels.post.html) |
| POST | `/guilds/{guild_id}/roles` | `postGuildsGuildIdRoles` | [创建频道身份组](https://bot.q.qq.com/wiki/develop/api-v2/server-inter/channel/role-group/post_guild_role.html) |
| POST | `/users/@me/dms` | `postUsersMeDms` | [频道私信](https://bot.q.qq.com/wiki/develop/api-v2/server-inter/channel/message/dms.html) |
| POST | `/v2/generate_url_link` | `postV2GenerateUrlLink` | [生成分享链接](https://bot.q.qq.com/wiki/develop/api-v2/autogen/api/v2_generate_url_link.post.html) |
| POST | `/v2/groups/join_approval_strategy` | `postV2GroupsJoinApprovalStrategy` | [创建入群自动审批策略](https://bot.q.qq.com/wiki/develop/api-v2/autogen/api/v2_groups_join_approval_strategy.post.html) |
| POST | `/v2/groups/join_approval_strategy/{strategy_id}/execute` | `postV2GroupsJoinApprovalStrategyStrategyIdExecute` | [执行入群自动审批策略](https://bot.q.qq.com/wiki/develop/api-v2/autogen/api/v2_groups_join_approval_strategy_strategy_id_execute.post.html) |
| POST | `/v2/groups/join_approval_strategy/{strategy_id}/whitelist_users` | `postV2GroupsJoinApprovalStrategyStrategyIdWhitelistUsers` | [修改入群自动审批策略的白名单号码](https://bot.q.qq.com/wiki/develop/api-v2/autogen/api/v2_groups_join_approval_strategy_strategy_id_whitelist_users.post.html) |
| POST | `/v2/groups/{group_id}/upload_part_finish` | `postV2GroupsGroupIdUploadPartFinish` | [群聊分片上传完成](https://bot.q.qq.com/wiki/develop/api-v2/autogen/api/v2_groups_group_id_upload_part_finish.post.html) |
| POST | `/v2/groups/{group_id}/upload_prepare` | `postV2GroupsGroupIdUploadPrepare` | [群聊富媒体预上传](https://bot.q.qq.com/wiki/develop/api-v2/autogen/api/v2_groups_group_id_upload_prepare.post.html) |
| POST | `/v2/groups/{group_openid}/approval_join_request/{member_openid}` | `postV2GroupsGroupOpenidApprovalJoinRequestMemberOpenid` | [入群申请审批](https://bot.q.qq.com/wiki/develop/api-v2/autogen/api/v2_groups_group_openid_approval_join_request_member_openid.post.html) |
| POST | `/v2/groups/{group_openid}/batch_remove_members` | `postV2GroupsGroupOpenidBatchRemoveMembers` | [群成员批量移除](https://bot.q.qq.com/wiki/develop/api-v2/autogen/api/v2_groups_group_openid_batch_remove_members.post.html) |
| POST | `/v2/groups/{group_openid}/files` | `postV2GroupsGroupOpenidFiles` | [群聊富媒体上传](https://bot.q.qq.com/wiki/develop/api-v2/autogen/api/v2_groups_group_openid_files.post.html) |
| POST | `/v2/groups/{group_openid}/member_blacklist` | `postV2GroupsGroupOpenidMemberBlacklist` | [群黑名单操作](https://bot.q.qq.com/wiki/develop/api-v2/autogen/api/v2_groups_group_openid_member_blacklist.post.html) |
| POST | `/v2/groups/{group_openid}/messages` | `postV2GroupsGroupOpenidMessages` | [发送群聊消息](https://bot.q.qq.com/wiki/develop/api-v2/autogen/api/v2_groups_group_openid_messages.post.html) |
| POST | `/v2/groups/{group_openid}/restrict_chat_setting` | `postV2GroupsGroupOpenidRestrictChatSetting` | [设置群成员禁言](https://bot.q.qq.com/wiki/develop/api-v2/autogen/api/v2_groups_group_openid_restrict_chat_setting.post.html) |
| POST | `/v2/media-images/complete` | `postV2MediaImagesComplete` | [素材管理](https://bot.q.qq.com/wiki/develop/api-v2/server-inter/message/material-management.html) |
| POST | `/v2/media-images/prepare` | `postV2MediaImagesPrepare` | [素材管理](https://bot.q.qq.com/wiki/develop/api-v2/server-inter/message/material-management.html) |
| POST | `/v2/panels` | `postV2Panels` | [创建指令面板](https://bot.q.qq.com/wiki/develop/api-v2/autogen/api/v2_panels.post.html) |
| POST | `/v2/users/{user_id}/upload_part_finish` | `postV2UsersUserIdUploadPartFinish` | [单聊分片上传完成](https://bot.q.qq.com/wiki/develop/api-v2/autogen/api/v2_users_user_id_upload_part_finish.post.html) |
| POST | `/v2/users/{user_id}/upload_prepare` | `postV2UsersUserIdUploadPrepare` | [单聊富媒体预上传](https://bot.q.qq.com/wiki/develop/api-v2/autogen/api/v2_users_user_id_upload_prepare.post.html) |
| POST | `/v2/users/{user_openid}/files` | `postV2UsersUserOpenidFiles` | [单聊富媒体上传](https://bot.q.qq.com/wiki/develop/api-v2/autogen/api/v2_users_user_openid_files.post.html) |
| POST | `/v2/users/{user_openid}/messages` | `postV2UsersUserOpenidMessages` | [发送单聊消息](https://bot.q.qq.com/wiki/develop/api-v2/autogen/api/v2_users_user_openid_messages.post.html) |
| POST | `/v2/users/{user_openid}/stream_messages` | `postV2UsersUserOpenidStreamMessages` | [流式发送单聊消息](https://bot.q.qq.com/wiki/develop/api-v2/autogen/api/v2_users_user_openid_stream_messages.post.html) |
| PUT | `/channels/{channel_id}/members/{user_id}/permissions` | `putChannelsChannelIdMembersUserIdPermissions` | [修改子频道用户权限](https://bot.q.qq.com/wiki/develop/api-v2/server-inter/channel/role-group/channel_permissions/put_channel_permissions.html) |
| PUT | `/channels/{channel_id}/messages/{message_id}/reactions/{type}/{id}` | `putChannelsChannelIdMessagesMessageIdReactionsTypeId` | [表情表态](https://bot.q.qq.com/wiki/develop/api-v2/server-inter/message/trans/emoji.html) |
| PUT | `/channels/{channel_id}/mic` | `putChannelsChannelIdMic` | [机器人上麦](https://bot.q.qq.com/wiki/develop/api-v2/server-inter/channel/content/audio/put_mic.html) |
| PUT | `/channels/{channel_id}/pins/{message_id}` | `putChannelsChannelIdPinsMessageId` | [添加精华消息](https://bot.q.qq.com/wiki/develop/api-v2/server-inter/channel/content/pins/put_pins_message.html) |
| PUT | `/channels/{channel_id}/roles/{role_id}/permissions` | `putChannelsChannelIdRolesRoleIdPermissions` | [修改子频道身份组权限](https://bot.q.qq.com/wiki/develop/api-v2/server-inter/channel/role-group/channel_permissions/put_channel_roles_permissions.html) |
| PUT | `/channels/{channel_id}/threads` | `putChannelsChannelIdThreads` | [发表帖子](https://bot.q.qq.com/wiki/develop/api-v2/server-inter/channel/content/forum/put_thread.html) |
| PUT | `/guilds/{guild_id}/members/{user_id}/roles/{role_id}` | `putGuildsGuildIdMembersUserIdRolesRoleId` | [创建频道身份组成员](https://bot.q.qq.com/wiki/develop/api-v2/server-inter/channel/role-group/put_guild_member_role.html) |
| PUT | `/interactions/{interaction_id}` | `putInteractionsInteractionId` | [互动事件响应](https://bot.q.qq.com/wiki/develop/api-v2/autogen/api/interactions_interaction_id.put.html) |
| PUT | `/v2/menu` | `putV2Menu` | [修改全局自定义菜单](https://bot.q.qq.com/wiki/develop/api-v2/autogen/api/v2_menu.put.html) |
| PUT | `/v2/panels/{panel_id}` | `putV2PanelsPanelId` | [修改指令面板](https://bot.q.qq.com/wiki/develop/api-v2/autogen/api/v2_panels_panel_id.put.html) |
| PUT | `/v2/panels/{panel_id}/target` | `putV2PanelsPanelIdTarget` | [修改指令面板关联对象](https://bot.q.qq.com/wiki/develop/api-v2/autogen/api/v2_panels_panel_id_target.put.html) |

## 事件

全部事件通过 `onEvent` 或 `on(EventType, ...)` 订阅，未知新事件同样保留原始数据。

| 事件 | 官方文档 |
|---|---|
| `AT_MESSAGE_CREATE` | [文档](https://bot.q.qq.com/wiki/develop/api-v2/dev-prepare/interface-framework/event-emit.html) |
| `AUDIO_FINISH` | [文档](https://bot.q.qq.com/wiki/develop/api-v2/dev-prepare/interface-framework/event-emit.html) |
| `AUDIO_OFF_MIC` | [文档](https://bot.q.qq.com/wiki/develop/api-v2/dev-prepare/interface-framework/event-emit.html) |
| `AUDIO_ON_MIC` | [文档](https://bot.q.qq.com/wiki/develop/api-v2/dev-prepare/interface-framework/event-emit.html) |
| `AUDIO_OR_LIVE_CHANNEL_MEMBER_ENTER` | [文档](https://bot.q.qq.com/wiki/develop/api-v2/server-inter/channel/role/audio_or_live_channel_member.html) |
| `AUDIO_OR_LIVE_CHANNEL_MEMBER_EXIT` | [文档](https://bot.q.qq.com/wiki/develop/api-v2/server-inter/channel/role/audio_or_live_channel_member.html) |
| `AUDIO_START` | [文档](https://bot.q.qq.com/wiki/develop/api-v2/dev-prepare/interface-framework/event-emit.html) |
| `C2C_MESSAGE_CREATE` | [文档](https://bot.q.qq.com/wiki/develop/api-v2/autogen/event/c2c_message_create.html) |
| `C2C_MSG_RECEIVE` | [文档](https://bot.q.qq.com/wiki/develop/api-v2/autogen/event/c2c_msg_receive.html) |
| `C2C_MSG_REJECT` | [文档](https://bot.q.qq.com/wiki/develop/api-v2/autogen/event/c2c_msg_reject.html) |
| `CHANNEL_CREATE` | [文档](https://bot.q.qq.com/wiki/develop/api-v2/autogen/event/channel_create.html) |
| `CHANNEL_DELETE` | [文档](https://bot.q.qq.com/wiki/develop/api-v2/autogen/event/channel_delete.html) |
| `CHANNEL_UPDATE` | [文档](https://bot.q.qq.com/wiki/develop/api-v2/autogen/event/channel_update.html) |
| `DIRECT_MESSAGE_CREATE` | [文档](https://bot.q.qq.com/wiki/develop/api-v2/dev-prepare/interface-framework/event-emit.html) |
| `DIRECT_MESSAGE_DELETE` | [文档](https://bot.q.qq.com/wiki/develop/api-v2/dev-prepare/interface-framework/event-emit.html) |
| `FORUM_POST_CREATE` | [文档](https://bot.q.qq.com/wiki/develop/api-v2/dev-prepare/interface-framework/event-emit.html) |
| `FORUM_POST_DELETE` | [文档](https://bot.q.qq.com/wiki/develop/api-v2/dev-prepare/interface-framework/event-emit.html) |
| `FORUM_PUBLISH_AUDIT_RESULT` | [文档](https://bot.q.qq.com/wiki/develop/api-v2/dev-prepare/interface-framework/event-emit.html) |
| `FORUM_REPLY_CREATE` | [文档](https://bot.q.qq.com/wiki/develop/api-v2/dev-prepare/interface-framework/event-emit.html) |
| `FORUM_REPLY_DELETE` | [文档](https://bot.q.qq.com/wiki/develop/api-v2/dev-prepare/interface-framework/event-emit.html) |
| `FORUM_THREAD_CREATE` | [文档](https://bot.q.qq.com/wiki/develop/api-v2/dev-prepare/interface-framework/event-emit.html) |
| `FORUM_THREAD_DELETE` | [文档](https://bot.q.qq.com/wiki/develop/api-v2/dev-prepare/interface-framework/event-emit.html) |
| `FORUM_THREAD_UPDATE` | [文档](https://bot.q.qq.com/wiki/develop/api-v2/dev-prepare/interface-framework/event-emit.html) |
| `FRIEND_ADD` | [文档](https://bot.q.qq.com/wiki/develop/api-v2/autogen/event/friend_add.html) |
| `FRIEND_DEL` | [文档](https://bot.q.qq.com/wiki/develop/api-v2/autogen/event/friend_del.html) |
| `GROUP_ADD_ROBOT` | [文档](https://bot.q.qq.com/wiki/develop/api-v2/autogen/event/group_add_robot.html) |
| `GROUP_AT_MESSAGE_CREATE` | [文档](https://bot.q.qq.com/wiki/develop/api-v2/autogen/event/group_at_message_create.html) |
| `GROUP_DEL_ROBOT` | [文档](https://bot.q.qq.com/wiki/develop/api-v2/autogen/event/group_del_robot.html) |
| `GROUP_JOIN_REQUEST` | [文档](https://bot.q.qq.com/wiki/develop/api-v2/autogen/event/group_join_request.html) |
| `GROUP_MEMBER_ADD` | [文档](https://bot.q.qq.com/wiki/develop/api-v2/autogen/event/group_member_add.html) |
| `GROUP_MEMBER_REMOVE` | [文档](https://bot.q.qq.com/wiki/develop/api-v2/autogen/event/group_member_remove.html) |
| `GROUP_MESSAGE_CREATE` | [文档](https://bot.q.qq.com/wiki/develop/api-v2/autogen/event/group_message_create.html) |
| `GROUP_MSG_RECEIVE` | [文档](https://bot.q.qq.com/wiki/develop/api-v2/autogen/event/group_msg_receive.html) |
| `GROUP_MSG_REJECT` | [文档](https://bot.q.qq.com/wiki/develop/api-v2/autogen/event/group_msg_reject.html) |
| `GUILD_CREATE` | [文档](https://bot.q.qq.com/wiki/develop/api-v2/autogen/event/guild_create.html) |
| `GUILD_DELETE` | [文档](https://bot.q.qq.com/wiki/develop/api-v2/autogen/event/guild_delete.html) |
| `GUILD_MEMBER_ADD` | [文档](https://bot.q.qq.com/wiki/develop/api-v2/server-inter/channel/role/guild_member.html) |
| `GUILD_MEMBER_REMOVE` | [文档](https://bot.q.qq.com/wiki/develop/api-v2/server-inter/channel/role/guild_member.html) |
| `GUILD_MEMBER_UPDATE` | [文档](https://bot.q.qq.com/wiki/develop/api-v2/server-inter/channel/role/guild_member.html) |
| `GUILD_UPDATE` | [文档](https://bot.q.qq.com/wiki/develop/api-v2/autogen/event/guild_update.html) |
| `INTERACTION_CREATE` | [文档](https://bot.q.qq.com/wiki/develop/api-v2/autogen/event/interaction_create.html) |
| `MESSAGE_AUDIT_PASS` | [文档](https://bot.q.qq.com/wiki/develop/api-v2/dev-prepare/interface-framework/event-emit.html) |
| `MESSAGE_AUDIT_REJECT` | [文档](https://bot.q.qq.com/wiki/develop/api-v2/dev-prepare/interface-framework/event-emit.html) |
| `MESSAGE_CREATE` | [文档](https://bot.q.qq.com/wiki/develop/api-v2/dev-prepare/interface-framework/event-emit.html) |
| `MESSAGE_DELETE` | [文档](https://bot.q.qq.com/wiki/develop/api-v2/dev-prepare/interface-framework/event-emit.html) |
| `MESSAGE_REACTION_ADD` | [文档](https://bot.q.qq.com/wiki/develop/api-v2/dev-prepare/interface-framework/event-emit.html) |
| `MESSAGE_REACTION_REMOVE` | [文档](https://bot.q.qq.com/wiki/develop/api-v2/dev-prepare/interface-framework/event-emit.html) |
| `PUBLIC_MESSAGE_DELETE` | [文档](https://bot.q.qq.com/wiki/develop/api-v2/dev-prepare/interface-framework/event-emit.html) |
| `SUBSCRIBE_MESSAGE_STATUS` | [文档](https://bot.q.qq.com/wiki/develop/api-v2/autogen/event/subscribe_message_status.html) |
