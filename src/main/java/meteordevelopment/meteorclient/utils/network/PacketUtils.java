/*
 * This file is part of the Meteor Client distribution (https://github.com/MeteorDevelopment/meteor-client/).
 * Copyright (c) Meteor Development.
 */

package meteordevelopment.meteorclient.utils.network;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Sets;
import it.unimi.dsi.fastutil.objects.Object2ReferenceOpenHashMap;
import it.unimi.dsi.fastutil.objects.Reference2ObjectOpenHashMap;
import net.minecraft.network.packet.CookiePackets;
import net.minecraft.network.packet.CommonPackets;
import net.minecraft.network.packet.ConfigPackets;
import net.minecraft.network.packet.HandshakePackets;
import net.minecraft.network.packet.LoginPackets;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.PacketType;
import net.minecraft.network.packet.PingPackets;
import net.minecraft.network.packet.PlayPackets;
import net.minecraft.network.packet.StatusPackets;
import net.minecraft.network.state.ConfigurationStates;
import net.minecraft.network.state.HandshakeStates;
import net.minecraft.network.state.LoginStates;
import net.minecraft.network.state.NetworkState;
import net.minecraft.network.state.PlayStateFactories;
import net.minecraft.network.state.QueryStates;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Map;
import java.util.Set;
import java.util.stream.Stream;

public class PacketUtils {
    private static final Map<Identifier, PacketType<? extends @NotNull Packet<?>>> CLIENTBOUND_PACKETS_MAP;
    private static final Map<Identifier, PacketType<? extends @NotNull Packet<?>>> SERVERBOUND_PACKETS_MAP;

    private static final Set<PacketType<? extends @NotNull Packet<?>>> CLIENTBOUND_PACKETS;
    private static final Set<PacketType<? extends @NotNull Packet<?>>> SERVERBOUND_PACKETS;

    public static Set<PacketType<? extends @NotNull Packet<?>>> getPackets() {
        return Sets.union(CLIENTBOUND_PACKETS, SERVERBOUND_PACKETS);
    }

    public static Set<PacketType<? extends Packet<?>>> getClientboundPackets() {
        return CLIENTBOUND_PACKETS;
    }

    public static Set<PacketType<? extends Packet<?>>> getServerboundPackets() {
        return SERVERBOUND_PACKETS;
    }

    public static @Nullable PacketType<? extends @NotNull Packet<?>> getClientboundPacket(Identifier id) {
        return CLIENTBOUND_PACKETS_MAP.get(id);
    }

    public static @Nullable PacketType<? extends @NotNull Packet<?>> getServerboundPacket(Identifier id) {
        return SERVERBOUND_PACKETS_MAP.get(id);
    }

    public static @Nullable PacketType<? extends @NotNull Packet<?>> getPacket(Identifier id) {
        @Nullable PacketType<? extends @NotNull Packet<?>> clientbound = getClientboundPacket(id);
        return clientbound != null ? clientbound : getServerboundPacket(id);
    }

    public static @Nullable PacketType<? extends @NotNull Packet<?>> getPacket(String name) {
        if (name.startsWith("clientbound/")) {
            @Nullable Identifier identifier = Identifier.tryParse(name.substring(12));
            return CLIENTBOUND_PACKETS_MAP.get(identifier);
        }

        if (name.startsWith("serverbound/")) {
            @Nullable Identifier identifier = Identifier.tryParse(name.substring(12));
            return SERVERBOUND_PACKETS_MAP.get(identifier);
        }

        @Nullable Identifier identifier = Identifier.tryParse(name);
        if (identifier != null) {
            @Nullable PacketType<? extends @NotNull Packet<?>> type = getPacket(identifier);
            if (type != null) return type;
        }

        return LEGACY_PACKET_MAPPINGS.get(name);
    }

    static {
        ImmutableMap.Builder<@NotNull Identifier, @NotNull PacketType<? extends @NotNull Packet<?>>> clientbound = ImmutableMap.builder();
        ImmutableMap.Builder<@NotNull Identifier, @NotNull PacketType<? extends @NotNull Packet<?>>> serverbound = ImmutableMap.builder();

        Stream.of(
                QueryStates.S2C_FACTORY,
                LoginStates.S2C_FACTORY,
                ConfigurationStates.S2C_FACTORY,
                PlayStateFactories.S2C
            ).map(NetworkState.Factory::buildUnbound)
            .forEach(details -> details.forEachPacketType((type, _protocolId) -> clientbound.put(type.id(), type)));

        Stream.of(
                HandshakeStates.C2S_FACTORY,
                QueryStates.C2S_FACTORY,
                LoginStates.C2S_FACTORY,
                ConfigurationStates.C2S_FACTORY,
                PlayStateFactories.C2S
            ).map(NetworkState.Factory::buildUnbound)
            .forEach(details -> details.forEachPacketType((type, _protocolId) -> serverbound.put(type.id(), type)));

        CLIENTBOUND_PACKETS_MAP = clientbound.buildKeepingLast();
        SERVERBOUND_PACKETS_MAP = serverbound.buildKeepingLast();

        CLIENTBOUND_PACKETS = Set.copyOf(CLIENTBOUND_PACKETS_MAP.values());
        SERVERBOUND_PACKETS = Set.copyOf(SERVERBOUND_PACKETS_MAP.values());
    }

    /**
     * Maps our legacy packet names to modern packet types.
     * @implNote Do not update keys or add entries, only update values.
     */
     private static final Map<String, PacketType<? extends @NotNull Packet<?>>> LEGACY_PACKET_MAPPINGS;

     static {
         ImmutableMap.Builder<@NotNull String, @NotNull PacketType<? extends @NotNull Packet<?>>> builder = ImmutableMap.builder();

        builder.put("AcceptCodeOfConductC2SPacket", ConfigPackets.ACCEPT_CODE_OF_CONDUCT);
        builder.put("AcknowledgeChunksC2SPacket", PlayPackets.CHUNK_BATCH_RECEIVED);
        builder.put("AcknowledgeReconfigurationC2SPacket", PlayPackets.CONFIGURATION_ACKNOWLEDGED);
        builder.put("AdvancementTabC2SPacket", PlayPackets.SEEN_ADVANCEMENTS);
        builder.put("BoatPaddleStateC2SPacket", PlayPackets.PADDLE_BOAT);
        builder.put("BookUpdateC2SPacket", PlayPackets.EDIT_BOOK);
        builder.put("BundleItemSelectedC2SPacket", PlayPackets.BUNDLE_ITEM_SELECTED);
        builder.put("ButtonClickC2SPacket", PlayPackets.CONTAINER_BUTTON_CLICK);
        builder.put("ChangeGameModeC2SPacket", PlayPackets.CHANGE_GAME_MODE);
        builder.put("ChatCommandSignedC2SPacket", PlayPackets.CHAT_COMMAND_SIGNED);
        builder.put("ChatMessageC2SPacket", PlayPackets.CHAT);
        builder.put("ClickSlotC2SPacket", PlayPackets.CONTAINER_CLICK);
        builder.put("ClientCommandC2SPacket", PlayPackets.PLAYER_COMMAND);
        builder.put("ClientOptionsC2SPacket", CommonPackets.CLIENT_INFORMATION);
        builder.put("ClientStatusC2SPacket", PlayPackets.CLIENT_COMMAND);
        builder.put("ClientTickEndC2SPacket", PlayPackets.CLIENT_TICK_END);
        builder.put("CloseHandledScreenC2SPacket", PlayPackets.CONTAINER_CLOSE_C2S);
        builder.put("CommandExecutionC2SPacket", PlayPackets.CHAT_COMMAND);
        builder.put("CommonPongC2SPacket", CommonPackets.PONG);
        builder.put("CookieResponseC2SPacket", CookiePackets.COOKIE_RESPONSE);
        builder.put("CraftRequestC2SPacket", PlayPackets.PLACE_RECIPE);
        builder.put("CreativeInventoryActionC2SPacket", PlayPackets.SET_CREATIVE_MODE_SLOT);
        builder.put("CustomClickActionC2SPacket", CommonPackets.CUSTOM_CLICK_ACTION);
        builder.put("CustomPayloadC2SPacket", CommonPackets.CUSTOM_PAYLOAD_C2S);
        builder.put("DebugSubscriptionRequestC2SPacket", PlayPackets.DEBUG_SUBSCRIPTION_REQUEST);
        builder.put("EnterConfigurationC2SPacket", LoginPackets.LOGIN_ACKNOWLEDGED);
        builder.put("HandSwingC2SPacket", PlayPackets.SWING);
        builder.put("HandshakeC2SPacket", HandshakePackets.INTENTION);
        builder.put("JigsawGeneratingC2SPacket", PlayPackets.JIGSAW_GENERATE);
        builder.put("KeepAliveC2SPacket", CommonPackets.KEEP_ALIVE_C2S);
        builder.put("LoginHelloC2SPacket", LoginPackets.HELLO_C2S);
        builder.put("LoginKeyC2SPacket", LoginPackets.KEY);
        builder.put("LoginQueryResponseC2SPacket", LoginPackets.CUSTOM_QUERY_ANSWER);
        builder.put("MessageAcknowledgmentC2SPacket", PlayPackets.CHAT_ACK);
        builder.put("PickItemFromBlockC2SPacket", PlayPackets.PICK_ITEM_FROM_BLOCK);
        builder.put("PickItemFromEntityC2SPacket", PlayPackets.PICK_ITEM_FROM_ENTITY);
        builder.put("PlayerActionC2SPacket", PlayPackets.PLAYER_ACTION);
        builder.put("PlayerInputC2SPacket", PlayPackets.PLAYER_INPUT);
        builder.put("PlayerInteractBlockC2SPacket", PlayPackets.USE_ITEM_ON);
        builder.put("PlayerInteractEntityC2SPacket", PlayPackets.INTERACT);
        builder.put("PlayerInteractItemC2SPacket", PlayPackets.USE_ITEM);
        builder.put("PlayerLoadedC2SPacket", PlayPackets.PLAYER_LOADED);
        builder.put("PlayerMoveC2SPacket.Full", PlayPackets.MOVE_PLAYER_POS_ROT);
        builder.put("PlayerMoveC2SPacket.LookAndOnGround", PlayPackets.MOVE_PLAYER_ROT);
        builder.put("PlayerMoveC2SPacket.OnGroundOnly", PlayPackets.MOVE_PLAYER_STATUS_ONLY);
        builder.put("PlayerMoveC2SPacket.PositionAndOnGround", PlayPackets.MOVE_PLAYER_POS);
        builder.put("PlayerSessionC2SPacket", PlayPackets.CHAT_SESSION_UPDATE);
        builder.put("QueryBlockNbtC2SPacket", PlayPackets.BLOCK_ENTITY_TAG_QUERY);
        builder.put("QueryEntityNbtC2SPacket", PlayPackets.ENTITY_TAG_QUERY);
        builder.put("QueryPingC2SPacket", PingPackets.PING_REQUEST);
        builder.put("QueryRequestC2SPacket", StatusPackets.STATUS_REQUEST);
        builder.put("ReadyC2SPacket", ConfigPackets.FINISH_CONFIGURATION_C2S);
        builder.put("RecipeBookDataC2SPacket", PlayPackets.RECIPE_BOOK_SEEN_RECIPE);
        builder.put("RecipeCategoryOptionsC2SPacket", PlayPackets.RECIPE_BOOK_CHANGE_SETTINGS);
        builder.put("RenameItemC2SPacket", PlayPackets.RENAME_ITEM);
        builder.put("RequestCommandCompletionsC2SPacket", PlayPackets.COMMAND_SUGGESTION);
        builder.put("ResourcePackStatusC2SPacket", CommonPackets.RESOURCE_PACK);
        builder.put("SelectKnownPacksC2SPacket", ConfigPackets.SELECT_KNOWN_PACKS_C2S);
        builder.put("SelectMerchantTradeC2SPacket", PlayPackets.SELECT_TRADE);
        builder.put("SetTestBlockC2SPacket", PlayPackets.SET_TEST_BLOCK);
        builder.put("SlotChangedStateC2SPacket", PlayPackets.CONTAINER_SLOT_STATE_CHANGED);
        builder.put("SpectatorTeleportC2SPacket", PlayPackets.TELEPORT_TO_ENTITY);
        builder.put("TeleportConfirmC2SPacket", PlayPackets.ACCEPT_TELEPORTATION);
        builder.put("TestInstanceBlockActionC2SPacket", PlayPackets.TEST_INSTANCE_BLOCK_ACTION);
        builder.put("UpdateBeaconC2SPacket", PlayPackets.SET_BEACON);
        builder.put("UpdateCommandBlockC2SPacket", PlayPackets.SET_COMMAND_BLOCK);
        builder.put("UpdateCommandBlockMinecartC2SPacket", PlayPackets.SET_COMMAND_MINECART);
        builder.put("UpdateDifficultyC2SPacket", PlayPackets.CHANGE_DIFFICULTY_C2S);
        builder.put("UpdateDifficultyLockC2SPacket", PlayPackets.LOCK_DIFFICULTY);
        builder.put("UpdateJigsawC2SPacket", PlayPackets.SET_JIGSAW_BLOCK);
        builder.put("UpdatePlayerAbilitiesC2SPacket", PlayPackets.PLAYER_ABILITIES_C2S);
        builder.put("UpdateSelectedSlotC2SPacket", PlayPackets.SET_CARRIED_ITEM_C2S);
        builder.put("UpdateSignC2SPacket", PlayPackets.SIGN_UPDATE);
        builder.put("UpdateStructureBlockC2SPacket", PlayPackets.SET_STRUCTURE_BLOCK);
        builder.put("VehicleMoveC2SPacket", PlayPackets.MOVE_VEHICLE_C2S);

        builder.put("AdvancementUpdateS2CPacket", PlayPackets.UPDATE_ADVANCEMENTS);
        builder.put("BlockBreakingProgressS2CPacket", PlayPackets.BLOCK_DESTRUCTION);
        builder.put("BlockEntityUpdateS2CPacket", PlayPackets.BLOCK_ENTITY_DATA);
        builder.put("BlockEventS2CPacket", PlayPackets.BLOCK_EVENT);
        builder.put("BlockUpdateS2CPacket", PlayPackets.BLOCK_UPDATE);
        builder.put("BlockValueDebugS2CPacket", PlayPackets.BLOCK_VALUE_DEBUG);
        builder.put("BossBarS2CPacket", PlayPackets.BOSS_EVENT);
        builder.put("ChatMessageS2CPacket", PlayPackets.PLAYER_CHAT);
        builder.put("ChatSuggestionsS2CPacket", PlayPackets.CUSTOM_CHAT_COMPLETIONS);
        builder.put("ChunkBiomeDataS2CPacket", PlayPackets.CHUNKS_BIOMES);
        builder.put("ChunkDataS2CPacket", PlayPackets.LEVEL_CHUNK_WITH_LIGHT);
        builder.put("ChunkDeltaUpdateS2CPacket", PlayPackets.SECTION_BLOCKS_UPDATE);
        builder.put("ChunkLoadDistanceS2CPacket", PlayPackets.SET_CHUNK_CACHE_RADIUS);
        builder.put("ChunkRenderDistanceCenterS2CPacket", PlayPackets.SET_CHUNK_CACHE_CENTER);
        builder.put("ChunkSentS2CPacket", PlayPackets.CHUNK_BATCH_FINISHED);
        builder.put("ChunkValueDebugS2CPacket", PlayPackets.CHUNK_VALUE_DEBUG);
        builder.put("ClearDialogS2CPacket", CommonPackets.CLEAR_DIALOG);
        builder.put("ClearTitleS2CPacket", PlayPackets.CLEAR_TITLES);
        builder.put("CloseScreenS2CPacket", PlayPackets.CONTAINER_CLOSE_S2C);
        builder.put("CodeOfConductS2CPacket", ConfigPackets.CODE_OF_CONDUCT);
        builder.put("CommandSuggestionsS2CPacket", PlayPackets.COMMAND_SUGGESTIONS);
        builder.put("CommandTreeS2CPacket", PlayPackets.COMMANDS);
        builder.put("CommonPingS2CPacket", CommonPackets.PING);
        builder.put("CookieRequestS2CPacket", CookiePackets.COOKIE_REQUEST);
        builder.put("CooldownUpdateS2CPacket", PlayPackets.COOLDOWN);
        builder.put("CraftFailedResponseS2CPacket", PlayPackets.PLACE_GHOST_RECIPE);
        builder.put("CustomPayloadS2CPacket", CommonPackets.CUSTOM_PAYLOAD_S2C);
        builder.put("CustomReportDetailsS2CPacket", CommonPackets.CUSTOM_REPORT_DETAILS);
        builder.put("DamageTiltS2CPacket", PlayPackets.HURT_ANIMATION);

        builder.put("DeathMessageS2CPacket", PlayPackets.PLAYER_COMBAT_KILL);

        builder.put("DebugSampleS2CPacket", PlayPackets.DEBUG_SAMPLE);

        builder.put("DifficultyS2CPacket", PlayPackets.CHANGE_DIFFICULTY_S2C);
        builder.put("DisconnectS2CPacket", CommonPackets.DISCONNECT);
        builder.put("DynamicRegistriesS2CPacket", ConfigPackets.REGISTRY_DATA);
        builder.put("EndCombatS2CPacket", PlayPackets.PLAYER_COMBAT_END);
        builder.put("EnterCombatS2CPacket", PlayPackets.PLAYER_COMBAT_ENTER);
        builder.put("EnterReconfigurationS2CPacket", PlayPackets.START_CONFIGURATION);
        builder.put("EntitiesDestroyS2CPacket", PlayPackets.REMOVE_ENTITIES);
        builder.put("EntityAnimationS2CPacket", PlayPackets.ANIMATE);
        builder.put("EntityAttachS2CPacket", PlayPackets.SET_ENTITY_LINK);
        builder.put("EntityAttributesS2CPacket", PlayPackets.UPDATE_ATTRIBUTES);
        builder.put("EntityDamageS2CPacket", PlayPackets.DAMAGE_EVENT);
        builder.put("EntityEquipmentUpdateS2CPacket", PlayPackets.SET_EQUIPMENT);
        builder.put("EntityPassengersSetS2CPacket", PlayPackets.SET_PASSENGERS);
        builder.put("EntityPositionS2CPacket", PlayPackets.TELEPORT_ENTITY);
        builder.put("EntityPositionSyncS2CPacket", PlayPackets.ENTITY_POSITION_SYNC);
        builder.put("EntityS2CPacket.MoveRelative", PlayPackets.MOVE_ENTITY_POS);
        builder.put("EntityS2CPacket.Rotate", PlayPackets.MOVE_ENTITY_ROT);
        builder.put("EntityS2CPacket.RotateAndMoveRelative", PlayPackets.MOVE_ENTITY_POS_ROT);
        builder.put("EntitySetHeadYawS2CPacket", PlayPackets.ROTATE_HEAD);
        builder.put("EntitySpawnS2CPacket", PlayPackets.ADD_ENTITY);
        builder.put("EntityStatusEffectS2CPacket", PlayPackets.UPDATE_MOB_EFFECT);
        builder.put("EntityStatusS2CPacket", PlayPackets.ENTITY_EVENT);
        builder.put("EntityTrackerUpdateS2CPacket", PlayPackets.SET_ENTITY_DATA);
        builder.put("EntityValueDebugS2CPacket", PlayPackets.ENTITY_VALUE_DEBUG);
        builder.put("EntityVelocityUpdateS2CPacket", PlayPackets.SET_ENTITY_MOTION);
        builder.put("EventDebugS2CPacket", PlayPackets.EVENT_DEBUG);
        builder.put("ExperienceBarUpdateS2CPacket", PlayPackets.SET_EXPERIENCE);
        builder.put("ExplosionS2CPacket", PlayPackets.EXPLODE);
        builder.put("FeaturesS2CPacket", ConfigPackets.UPDATE_ENABLED_FEATURES);
        builder.put("GameJoinS2CPacket", PlayPackets.LOGIN);
        builder.put("GameMessageS2CPacket", PlayPackets.SYSTEM_CHAT);
        builder.put("GameStateChangeS2CPacket", PlayPackets.GAME_EVENT);
        builder.put("GameTestHighlightPosS2CPacket", PlayPackets.GAME_TEST_HIGHLIGHT_POS);
        builder.put("HealthUpdateS2CPacket", PlayPackets.SET_HEALTH);
        builder.put("InventoryS2CPacket", PlayPackets.CONTAINER_SET_CONTENT);
        builder.put("ItemPickupAnimationS2CPacket", PlayPackets.TAKE_ITEM_ENTITY);
        builder.put("KeepAliveS2CPacket", CommonPackets.KEEP_ALIVE_S2C);
        builder.put("LightUpdateS2CPacket", PlayPackets.LIGHT_UPDATE);
        builder.put("LoginCompressionS2CPacket", LoginPackets.LOGIN_COMPRESSION);
        builder.put("LoginDisconnectS2CPacket", LoginPackets.LOGIN_DISCONNECT);
        builder.put("LoginHelloS2CPacket", LoginPackets.HELLO_S2C);
        builder.put("LoginQueryRequestS2CPacket", LoginPackets.CUSTOM_QUERY);
        builder.put("LoginSuccessS2CPacket", LoginPackets.LOGIN_FINISHED);
        builder.put("LookAtS2CPacket", PlayPackets.PLAYER_LOOK_AT);
        builder.put("MapUpdateS2CPacket", PlayPackets.MAP_ITEM_DATA);
        builder.put("MoveMinecartAlongTrackS2CPacket", PlayPackets.MOVE_MINECART_ALONG_TRACK);
        builder.put("NbtQueryResponseS2CPacket", PlayPackets.TAG_QUERY);
        builder.put("OpenMountScreenS2CPacket", PlayPackets.MOUNT_SCREEN_OPEN);
        builder.put("OpenScreenS2CPacket", PlayPackets.OPEN_SCREEN);
        builder.put("OpenWrittenBookS2CPacket", PlayPackets.OPEN_BOOK);
        builder.put("OverlayMessageS2CPacket", PlayPackets.SET_ACTION_BAR_TEXT);
        builder.put("ParticleS2CPacket", PlayPackets.LEVEL_PARTICLES);
        builder.put("PingResultS2CPacket", PingPackets.PONG_RESPONSE);
        builder.put("PlaySoundFromEntityS2CPacket", PlayPackets.SOUND_ENTITY);
        builder.put("PlaySoundS2CPacket", PlayPackets.SOUND);
        builder.put("PlayerAbilitiesS2CPacket", PlayPackets.PLAYER_ABILITIES_S2C);
        builder.put("PlayerActionResponseS2CPacket", PlayPackets.BLOCK_CHANGED_ACK);
        builder.put("PlayerListHeaderS2CPacket", PlayPackets.TAB_LIST);
        builder.put("PlayerListS2CPacket", PlayPackets.PLAYER_INFO_UPDATE);
        builder.put("PlayerPositionLookS2CPacket", PlayPackets.PLAYER_POSITION);
        builder.put("PlayerRemoveS2CPacket", PlayPackets.PLAYER_INFO_REMOVE);
        builder.put("PlayerRespawnS2CPacket", PlayPackets.RESPAWN);
        builder.put("PlayerRotationS2CPacket", PlayPackets.PLAYER_ROTATION);
        builder.put("PlayerSpawnPositionS2CPacket", PlayPackets.SET_DEFAULT_SPAWN_POSITION);
        builder.put("ProfilelessChatMessageS2CPacket", PlayPackets.DISGUISED_CHAT);
        builder.put("ProjectilePowerS2CPacket", PlayPackets.PROJECTILE_POWER);
        builder.put("QueryResponseS2CPacket", StatusPackets.STATUS_RESPONSE);
        builder.put("ReadyS2CPacket", ConfigPackets.FINISH_CONFIGURATION_S2C);
        builder.put("RecipeBookAddS2CPacket", PlayPackets.RECIPE_BOOK_ADD);
        builder.put("RecipeBookRemoveS2CPacket", PlayPackets.RECIPE_BOOK_REMOVE);
        builder.put("RecipeBookSettingsS2CPacket", PlayPackets.RECIPE_BOOK_SETTINGS);
        builder.put("RemoveEntityStatusEffectS2CPacket", PlayPackets.REMOVE_MOB_EFFECT);
        builder.put("RemoveMessageS2CPacket", PlayPackets.DELETE_CHAT);
        builder.put("ResetChatS2CPacket", ConfigPackets.RESET_CHAT);
        builder.put("ResourcePackRemoveS2CPacket", CommonPackets.RESOURCE_PACK_POP);
        builder.put("ResourcePackSendS2CPacket", CommonPackets.RESOURCE_PACK_PUSH);
        builder.put("ScoreboardDisplayS2CPacket", PlayPackets.SET_DISPLAY_OBJECTIVE);
        builder.put("ScoreboardObjectiveUpdateS2CPacket", PlayPackets.SET_OBJECTIVE);
        builder.put("ScoreboardScoreResetS2CPacket", PlayPackets.RESET_SCORE);
        builder.put("ScoreboardScoreUpdateS2CPacket", PlayPackets.SET_SCORE);
        builder.put("ScreenHandlerPropertyUpdateS2CPacket", PlayPackets.CONTAINER_SET_DATA);
        builder.put("ScreenHandlerSlotUpdateS2CPacket", PlayPackets.CONTAINER_SET_SLOT);
        builder.put("SelectAdvancementTabS2CPacket", PlayPackets.SELECT_ADVANCEMENTS_TAB);
        builder.put("SelectKnownPacksS2CPacket", ConfigPackets.SELECT_KNOWN_PACKS_S2C);
        builder.put("ServerLinksS2CPacket", CommonPackets.SERVER_LINKS);
        builder.put("ServerMetadataS2CPacket", PlayPackets.SERVER_DATA);
        builder.put("ServerTransferS2CPacket", CommonPackets.TRANSFER);
        builder.put("SetCameraEntityS2CPacket", PlayPackets.SET_CAMERA);
        builder.put("SetCursorItemS2CPacket", PlayPackets.SET_CURSOR_ITEM);
        builder.put("SetPlayerInventoryS2CPacket", PlayPackets.SET_PLAYER_INVENTORY);
        builder.put("SetTradeOffersS2CPacket", PlayPackets.MERCHANT_OFFERS);
        builder.put("ShowDialogS2CPacket", CommonPackets.SHOW_DIALOG);
        builder.put("SignEditorOpenS2CPacket", PlayPackets.OPEN_SIGN_EDITOR);
        builder.put("SimulationDistanceS2CPacket", PlayPackets.SET_SIMULATION_DISTANCE);
        builder.put("StartChunkSendS2CPacket", PlayPackets.CHUNK_BATCH_START);
        builder.put("StatisticsS2CPacket", PlayPackets.AWARD_STATS);
        builder.put("StopSoundS2CPacket", PlayPackets.STOP_SOUND);
        builder.put("StoreCookieS2CPacket", CommonPackets.STORE_COOKIE);
        builder.put("SubtitleS2CPacket", PlayPackets.SET_SUBTITLE_TEXT);
        builder.put("SynchronizeRecipesS2CPacket", PlayPackets.UPDATE_RECIPES);
        builder.put("SynchronizeTagsS2CPacket", CommonPackets.UPDATE_TAGS);
        builder.put("TeamS2CPacket", PlayPackets.SET_PLAYER_TEAM);
        builder.put("TestInstanceBlockStatusS2CPacket", PlayPackets.TEST_INSTANCE_BLOCK_STATUS);
        builder.put("TickStepS2CPacket", PlayPackets.TICKING_STEP);
        builder.put("TitleFadeS2CPacket", PlayPackets.SET_TITLES_ANIMATION);
        builder.put("TitleS2CPacket", PlayPackets.SET_TITLE_TEXT);
        builder.put("UnloadChunkS2CPacket", PlayPackets.FORGET_LEVEL_CHUNK);
        builder.put("UpdateSelectedSlotS2CPacket", PlayPackets.SET_CARRIED_ITEM_S2C);
        builder.put("UpdateTickRateS2CPacket", PlayPackets.TICKING_STATE);
        builder.put("VehicleMoveS2CPacket", PlayPackets.MOVE_VEHICLE_S2C);
        builder.put("WaypointS2CPacket", PlayPackets.WAYPOINT);
        builder.put("WorldBorderCenterChangedS2CPacket", PlayPackets.SET_BORDER_CENTER);
        builder.put("WorldBorderInitializeS2CPacket", PlayPackets.INITIALIZE_BORDER);
        builder.put("WorldBorderInterpolateSizeS2CPacket", PlayPackets.SET_BORDER_LERP_SIZE);
        builder.put("WorldBorderSizeChangedS2CPacket", PlayPackets.SET_BORDER_SIZE);
        builder.put("WorldBorderWarningBlocksChangedS2CPacket", PlayPackets.SET_BORDER_WARNING_DISTANCE);
        builder.put("WorldBorderWarningTimeChangedS2CPacket", PlayPackets.SET_BORDER_WARNING_DELAY);
        builder.put("WorldEventS2CPacket", PlayPackets.LEVEL_EVENT);
        builder.put("WorldTimeUpdateS2CPacket", PlayPackets.SET_TIME);
        LEGACY_PACKET_MAPPINGS = builder.buildOrThrow();
    }

    private PacketUtils() {}
}
