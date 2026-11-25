package com.community.module.social;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;

/**
 * 社交模块接口集成测试（MockMvc + H2 + Flyway）。
 *
 * <p>覆盖本周交付的验收点：关注/取关、互关判定、个人主页聚合、关注/粉丝列表、越权与异常分支。</p>
 */
@SpringBootTest
@AutoConfigureMockMvc
class SocialApiIntegrationTest {

    private static final long XIAOYU = 1L;
    private static final long ALAN = 2L;
    private static final long XINGYE = 3L;

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @BeforeEach
    void resetFollows() {
        // 每个用例从同一份种子数据出发：只有 阿岚 -> 小屿 这一条单向关注
        jdbcTemplate.update("DELETE FROM user_follows");
        jdbcTemplate.update("INSERT INTO user_follows (follower_id, followee_id, is_mutual) VALUES (?, ?, FALSE)",
                ALAN, XIAOYU);
    }

    @Test
    @DisplayName("游客可查看他人主页：返回资料、粉丝数、关注数，关注状态均为 false")
    void guestCanViewProfile() throws Exception {
        JsonNode data = okData(get("/api/v1/users/{id}/profile", XIAOYU));

        assertThat(data.get("nickname").asText()).isEqualTo("小屿");
        assertThat(data.get("followerCount").asLong()).isEqualTo(1);
        assertThat(data.get("followingCount").asLong()).isZero();
        assertThat(data.get("isSelf").asBoolean()).isFalse();
        assertThat(data.get("isFollowing").asBoolean()).isFalse();
        assertThat(data.get("isMutual").asBoolean()).isFalse();
    }

    @Test
    @DisplayName("关注后：主页关注状态为已关注，双向已关注时 isMutual = true，粉丝数加一")
    void followCreatesMutualRelation() throws Exception {
        JsonNode state = okData(post("/api/v1/users/{id}/follow", ALAN).header("Authorization", bearer("xiaoyu")));

        assertThat(state.get("isFollowing").asBoolean()).isTrue();
        assertThat(state.get("isMutual").asBoolean()).isTrue();
        assertThat(state.get("followerCount").asLong()).isEqualTo(1);

        JsonNode profile = okData(get("/api/v1/users/{id}/profile", ALAN).header("Authorization", bearer("xiaoyu")));
        assertThat(profile.get("isFollowing").asBoolean()).isTrue();
        assertThat(profile.get("isMutual").asBoolean()).isTrue();
        assertThat(profile.get("followerCount").asLong()).isEqualTo(1);

        // 对方视角同样是互关
        JsonNode reverse = okData(get("/api/v1/users/{id}/profile", XIAOYU).header("Authorization", bearer("alan")));
        assertThat(reverse.get("isFollowing").asBoolean()).isTrue();
        assertThat(reverse.get("isMutual").asBoolean()).isTrue();
    }

    @Test
    @DisplayName("重复关注幂等：不新增记录、不重复计数")
    void followIsIdempotent() throws Exception {
        okData(post("/api/v1/users/{id}/follow", ALAN).header("Authorization", bearer("xiaoyu")));
        JsonNode second = okData(post("/api/v1/users/{id}/follow", ALAN).header("Authorization", bearer("xiaoyu")));

        assertThat(second.get("isFollowing").asBoolean()).isTrue();
        assertThat(second.get("followerCount").asLong()).isEqualTo(1);
        Integer rows = jdbcTemplate.queryForObject(
                "SELECT COUNT(1) FROM user_follows WHERE follower_id = ? AND followee_id = ?",
                Integer.class, XIAOYU, ALAN);
        assertThat(rows).isEqualTo(1);
    }

    @Test
    @DisplayName("取关后：互关标记双向回退，好友可见性随之失效")
    void unfollowClearsMutualFlag() throws Exception {
        okData(post("/api/v1/users/{id}/follow", ALAN).header("Authorization", bearer("xiaoyu")));

        JsonNode state = okData(delete("/api/v1/users/{id}/follow", ALAN).header("Authorization", bearer("xiaoyu")));
        assertThat(state.get("isFollowing").asBoolean()).isFalse();
        assertThat(state.get("isMutual").asBoolean()).isFalse();
        assertThat(state.get("followerCount").asLong()).isZero();

        // 对方仍然关注我，但互关标记必须回退为 false
        Boolean reverseMutual = jdbcTemplate.queryForObject(
                "SELECT is_mutual FROM user_follows WHERE follower_id = ? AND followee_id = ?",
                Boolean.class, ALAN, XIAOYU);
        assertThat(reverseMutual).isFalse();
    }

    @Test
    @DisplayName("不能关注自己：返回 400 / 50001")
    void cannotFollowSelf() throws Exception {
        MvcResult result = mockMvc.perform(post("/api/v1/users/{id}/follow", XIAOYU)
                        .header("Authorization", bearer("xiaoyu")))
                .andReturn();

        assertThat(result.getResponse().getStatus()).isEqualTo(400);
        JsonNode body = readBody(result);
        assertThat(body.get("code").asInt()).isEqualTo(50001);
    }

    @Test
    @DisplayName("关注不存在的用户：返回 404 / 50002")
    void cannotFollowMissingUser() throws Exception {
        MvcResult result = mockMvc.perform(post("/api/v1/users/{id}/follow", 9999)
                        .header("Authorization", bearer("xiaoyu")))
                .andReturn();

        assertThat(result.getResponse().getStatus()).isEqualTo(404);
        assertThat(readBody(result).get("code").asInt()).isEqualTo(50002);
    }

    @Test
    @DisplayName("未登录不能关注：返回 401 / 20001")
    void guestCannotFollow() throws Exception {
        MvcResult result = mockMvc.perform(post("/api/v1/users/{id}/follow", ALAN)).andReturn();

        assertThat(result.getResponse().getStatus()).isEqualTo(401);
        assertThat(readBody(result).get("code").asInt()).isEqualTo(20001);
    }

    @Test
    @DisplayName("粉丝列表：返回分页结构，并带上访问者自己的关注状态")
    void followersListCarriesViewerState() throws Exception {
        JsonNode page = okData(get("/api/v1/users/{id}/followers", XIAOYU)
                .param("page", "1")
                .param("pageSize", "10")
                .header("Authorization", bearer("alan")));

        assertThat(page.get("total").asLong()).isEqualTo(1);
        assertThat(page.get("page").asInt()).isEqualTo(1);
        assertThat(page.get("pageSize").asInt()).isEqualTo(10);
        assertThat(page.get("hasMore").asBoolean()).isFalse();

        JsonNode item = page.get("items").get(0);
        assertThat(item.get("nickname").asText()).isEqualTo("阿岚");
        // 阿岚自己在看自己的关注列表：不把自己算作「已关注」
        assertThat(item.get("isFollowing").asBoolean()).isFalse();
    }

    @Test
    @DisplayName("关注列表：按关注时间倒序分页")
    void followingListPaged() throws Exception {
        okData(post("/api/v1/users/{id}/follow", XINGYE).header("Authorization", bearer("xiaoyu")));

        JsonNode page = okData(get("/api/v1/users/{id}/following", XIAOYU)
                .param("page", "1")
                .param("pageSize", "10")
                .header("Authorization", bearer("xiaoyu")));

        assertThat(page.get("total").asLong()).isEqualTo(1);
        assertThat(page.get("items").get(0).get("nickname").asText()).isEqualTo("星野");
        assertThat(page.get("items").get(0).get("isFollowing").asBoolean()).isTrue();
    }

    @Test
    @DisplayName("分页参数非法：返回 400 / 10001")
    void invalidPagingRejected() throws Exception {
        MvcResult result = mockMvc.perform(get("/api/v1/users/{id}/followers", XIAOYU).param("page", "0"))
                .andReturn();

        assertThat(result.getResponse().getStatus()).isEqualTo(400);
        assertThat(readBody(result).get("code").asInt()).isEqualTo(10001);
    }

    // ── helpers ──────────────────────────────────────────────────────────────

    private String bearer(String username) throws Exception {
        MvcResult result = mockMvc.perform(post("/api/v1/auth/demo-login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"username\":\"" + username + "\"}"))
                .andReturn();
        JsonNode body = readBody(result);
        assertThat(body.get("code").asInt()).as("演示登录应成功: " + body).isZero();
        return "Bearer " + body.get("data").get("token").asText();
    }

    private JsonNode okData(org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder builder)
            throws Exception {
        MvcResult result = mockMvc.perform(builder).andReturn();
        JsonNode body = readBody(result);
        assertThat(result.getResponse().getStatus()).as("HTTP 状态: " + body).isEqualTo(200);
        assertThat(body.get("code").asInt()).as("业务码: " + body).isZero();
        assertThat(body.get("data")).isNotNull();
        return body.get("data");
    }

    private JsonNode readBody(MvcResult result) throws Exception {
        return objectMapper.readTree(result.getResponse().getContentAsString(java.nio.charset.StandardCharsets.UTF_8));
    }
}
