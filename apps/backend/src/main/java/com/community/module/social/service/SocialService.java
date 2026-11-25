package com.community.module.social.service;

import com.community.common.api.PageResult;
import com.community.common.error.BusinessException;
import com.community.common.error.ErrorCode;
import com.community.module.social.dto.FollowStateVO;
import com.community.module.social.dto.UserBriefVO;
import com.community.module.social.dto.UserProfileVO;
import com.community.module.social.repository.FollowRow;
import com.community.module.social.repository.UserFollowRepository;
import com.community.module.social.spi.SocialUser;
import com.community.module.social.spi.SocialUserDirectory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 社交与好友圈核心业务：关注/取关、个人主页聚合、关注/粉丝列表。
 *
 * <p>业务规则（对应设计文档 3.2「互关即好友」）：</p>
 * <ul>
 *   <li>关注是单向关系，一行记录；A→B 与 B→A 同时存在即互关（好友）</li>
 *   <li>不能关注自己（50001）；目标用户不存在或已停用（50002）</li>
 *   <li>重复关注幂等：不新增记录，也不报错</li>
 *   <li>取关后，反向关系的 is_mutual 立即回退为 false，好友可见性随之失效</li>
 * </ul>
 */
@Service
public class SocialService {

    private static final int MAX_PAGE_SIZE = 50;
    /** 游客的伪用户 id：与任何真实用户都不相等，用于列表查询里计算「我是否已关注」。 */
    private static final long GUEST_VIEWER_ID = 0L;

    private final UserFollowRepository followRepository;
    private final SocialUserDirectory userDirectory;

    public SocialService(UserFollowRepository followRepository, SocialUserDirectory userDirectory) {
        this.followRepository = followRepository;
        this.userDirectory = userDirectory;
    }

    /** 个人主页聚合：用户资料 + 关注数/粉丝数 + 相对访问者的关注状态。 */
    public UserProfileVO getProfile(long targetUserId, Long viewerId) {
        SocialUser user = requireActiveUser(targetUserId);

        boolean isSelf = viewerId != null && viewerId == targetUserId;
        boolean isFollowing = viewerId != null && !isSelf && followRepository.exists(viewerId, targetUserId);
        boolean isMutual = isFollowing && followRepository.isMutual(viewerId, targetUserId);

        return new UserProfileVO(
                user.id(),
                user.username(),
                user.nickname(),
                user.avatarUrl(),
                user.bio(),
                followRepository.countFollowers(targetUserId),
                followRepository.countFollowing(targetUserId),
                isSelf,
                isFollowing,
                isMutual);
    }

    /** 关注某人（幂等）。 */
    @Transactional
    public FollowStateVO follow(long currentUserId, long targetUserId) {
        if (currentUserId == targetUserId) {
            throw new BusinessException(ErrorCode.CANNOT_FOLLOW_SELF);
        }
        requireActiveUser(targetUserId);

        followRepository.insertIfAbsent(currentUserId, targetUserId);

        // 对方已经关注我 → 双方都置为互关；否则我这条保持单向
        boolean mutual = followRepository.exists(targetUserId, currentUserId);
        followRepository.updateMutual(currentUserId, targetUserId, mutual);
        if (mutual) {
            followRepository.updateMutual(targetUserId, currentUserId, true);
        }

        return new FollowStateVO(true, mutual, followRepository.countFollowers(targetUserId));
    }

    /** 取关（幂等）：删除我的关注行，并把反向关系的互关标记回退。 */
    @Transactional
    public FollowStateVO unfollow(long currentUserId, long targetUserId) {
        requireActiveUser(targetUserId);

        followRepository.delete(currentUserId, targetUserId);
        followRepository.updateMutual(targetUserId, currentUserId, false);

        return new FollowStateVO(false, false, followRepository.countFollowers(targetUserId));
    }

    /** 粉丝列表：关注了 targetUserId 的人。 */
    public PageResult<UserBriefVO> listFollowers(long targetUserId, Long viewerId, int page, int pageSize) {
        requireActiveUser(targetUserId);
        int safePage = normalizePage(page);
        int safePageSize = normalizePageSize(pageSize);

        long total = followRepository.countFollowers(targetUserId);
        List<FollowRow> rows = followRepository.pageFollowers(
                targetUserId, viewerId == null ? GUEST_VIEWER_ID : viewerId,
                (long) (safePage - 1) * safePageSize, safePageSize);
        return PageResult.of(rows.stream().map(SocialService::toBrief).toList(), safePage, safePageSize, total);
    }

    /** 关注列表：targetUserId 关注的人。 */
    public PageResult<UserBriefVO> listFollowing(long targetUserId, Long viewerId, int page, int pageSize) {
        requireActiveUser(targetUserId);
        int safePage = normalizePage(page);
        int safePageSize = normalizePageSize(pageSize);

        long total = followRepository.countFollowing(targetUserId);
        List<FollowRow> rows = followRepository.pageFollowing(
                targetUserId, viewerId == null ? GUEST_VIEWER_ID : viewerId,
                (long) (safePage - 1) * safePageSize, safePageSize);
        return PageResult.of(rows.stream().map(SocialService::toBrief).toList(), safePage, safePageSize, total);
    }

    private SocialUser requireActiveUser(long userId) {
        return userDirectory.findById(userId)
                .filter(SocialUser::active)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));
    }

    private static UserBriefVO toBrief(FollowRow row) {
        return new UserBriefVO(row.userId(), row.username(), row.nickname(),
                row.avatarUrl(), row.bio(), row.following(), row.mutual());
    }

    private static int normalizePage(int page) {
        if (page < 1) {
            throw new BusinessException(ErrorCode.PARAM_INVALID, "page 必须大于等于 1");
        }
        return page;
    }

    private static int normalizePageSize(int pageSize) {
        if (pageSize < 1 || pageSize > MAX_PAGE_SIZE) {
            throw new BusinessException(ErrorCode.PARAM_INVALID, "pageSize 必须在 1~" + MAX_PAGE_SIZE + " 之间");
        }
        return pageSize;
    }
}
