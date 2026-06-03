package com.dhairya.Placement_management_system.insights;

import com.dhairya.Placement_management_system.application.ApplicationRepository;
import com.dhairya.Placement_management_system.common.exception.BusinessException;
import com.dhairya.Placement_management_system.drive.Drive;
import com.dhairya.Placement_management_system.drive.DriveRepository;
import com.dhairya.Placement_management_system.insights.dto.JobPostOverviewDTO;
import com.dhairya.Placement_management_system.insights.dto.ScoreDistributionDTO;
import com.dhairya.Placement_management_system.insights.dto.SkillGapDTO;
import com.dhairya.Placement_management_system.insights.dto.StatusCountDTO;
import com.dhairya.Placement_management_system.jobpost.JobPost;
import com.dhairya.Placement_management_system.jobpost.JobPostRepository;
import com.dhairya.Placement_management_system.user.ParsedResume;
import com.dhairya.Placement_management_system.user.ParsedResumeRepository;
import com.dhairya.Placement_management_system.user.User;
import com.dhairya.Placement_management_system.user.UserRepository;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class InsightsService {

    private final JobPostRepository jobPostRepository;
    private final DriveRepository driveRepository;
    private final ApplicationRepository applicationRepository;
    private final ParsedResumeRepository parsedResumeRepository;
    private final UserRepository userRepository;

    public InsightsService(JobPostRepository jobPostRepository,
                           DriveRepository driveRepository,
                           ApplicationRepository applicationRepository,
                           ParsedResumeRepository parsedResumeRepository,
                           UserRepository userRepository) {
        this.jobPostRepository = jobPostRepository;
        this.driveRepository = driveRepository;
        this.applicationRepository = applicationRepository;
        this.parsedResumeRepository = parsedResumeRepository;
        this.userRepository = userRepository;
    }

    public List<SkillGapDTO> getSkillGaps(UUID currentUserId) {
        User user = userRepository.findById(currentUserId)
            .orElseThrow(() -> new com.dhairya.Placement_management_system.common.exception.ResourceNotFoundException("User", "id", currentUserId));

        Set<String> requiredSkills = new HashSet<>();
        if ("ROLE_RECRUITER".equals(user.getRole()) || "RECRUITER".equals(user.getRole())) {
            List<JobPost> posts = jobPostRepository.findByRecruiterId(currentUserId);
            for (JobPost post : posts) {
                if (post.getDrive().getRequiredSkills() != null) {
                    requiredSkills.addAll(Arrays.asList(post.getDrive().getRequiredSkills()));
                }
            }
        } else if ("ROLE_PO".equals(user.getRole()) || "PO".equals(user.getRole())) {
            List<Drive> drives = driveRepository.findByCreatedById(currentUserId);
            for (Drive drive : drives) {
                if (drive.getRequiredSkills() != null) {
                    requiredSkills.addAll(Arrays.asList(drive.getRequiredSkills()));
                }
            }
        } else {
            List<JobPost> allPosts = jobPostRepository.findAll();
            for (JobPost post : allPosts) {
                if (post.getDrive().getRequiredSkills() != null) {
                    requiredSkills.addAll(Arrays.asList(post.getDrive().getRequiredSkills()));
                }
            }
        }

        List<ParsedResume> allParsed = parsedResumeRepository.findAll();
        Set<String> matchedSkills = new HashSet<>();
        for (ParsedResume pr : allParsed) {
            if (pr.getExtractedSkills() != null) {
                String[] skills = pr.getExtractedSkills().split(",");
                for (String s : skills) {
                    matchedSkills.add(s.trim().toLowerCase());
                }
            }
        }

        List<SkillGapDTO> result = new ArrayList<>();
        for (String skill : requiredSkills) {
            long required = countRequiredSkill(skill, user, currentUserId);
            long matched = countMatchedSkill(skill, matchedSkills);
            double gap = required > 0 ? ((double) (required - matched) / required) * 100 : 0;
            result.add(new SkillGapDTO(skill, required, matched, Math.max(0, gap)));
        }

        result.sort(Comparator.comparingDouble(SkillGapDTO::getGapPercentage).reversed());
        return result;
    }

    private long countRequiredSkill(String skill, User user, UUID userId) {
        long count = 0;
        if ("ROLE_RECRUITER".equals(user.getRole()) || "RECRUITER".equals(user.getRole())) {
            List<JobPost> posts = jobPostRepository.findByRecruiterId(userId);
            for (JobPost post : posts) {
                if (post.getDrive().getRequiredSkills() != null) {
                    for (String s : post.getDrive().getRequiredSkills()) {
                        if (s.equalsIgnoreCase(skill)) count++;
                    }
                }
            }
        } else if ("ROLE_PO".equals(user.getRole()) || "PO".equals(user.getRole())) {
            List<Drive> drives = driveRepository.findByCreatedById(userId);
            for (Drive drive : drives) {
                if (drive.getRequiredSkills() != null) {
                    for (String s : drive.getRequiredSkills()) {
                        if (s.equalsIgnoreCase(skill)) count++;
                    }
                }
            }
        } else {
            List<JobPost> posts = jobPostRepository.findAll();
            for (JobPost post : posts) {
                if (post.getDrive().getRequiredSkills() != null) {
                    for (String s : post.getDrive().getRequiredSkills()) {
                        if (s.equalsIgnoreCase(skill)) count++;
                    }
                }
            }
        }
        return count;
    }

    private long countMatchedSkill(String skill, Set<String> matchedSkills) {
        return matchedSkills.contains(skill.toLowerCase()) ? 1 : 0;
    }

    public JobPostOverviewDTO getOverview(UUID currentUserId) {
        User user = userRepository.findById(currentUserId)
            .orElseThrow(() -> new com.dhairya.Placement_management_system.common.exception.ResourceNotFoundException("User", "id", currentUserId));

        List<UUID> jobPostIds;
        if ("ROLE_RECRUITER".equals(user.getRole()) || "RECRUITER".equals(user.getRole())) {
            jobPostIds = jobPostRepository.findByRecruiterId(currentUserId).stream()
                .map(JobPost::getId).collect(Collectors.toList());
        } else if ("ROLE_PO".equals(user.getRole()) || "PO".equals(user.getRole())) {
            List<Drive> drives = driveRepository.findByCreatedById(currentUserId);
            jobPostIds = jobPostRepository.findByDriveIdIn(
                drives.stream().map(Drive::getId).collect(Collectors.toList())
            ).stream().map(JobPost::getId).collect(Collectors.toList());
        } else {
            jobPostIds = jobPostRepository.findAll().stream()
                .map(JobPost::getId).collect(Collectors.toList());
        }

        if (jobPostIds.isEmpty()) {
            return new JobPostOverviewDTO(List.of(), List.of());
        }

        List<Object[]> distribution = applicationRepository.getScoreDistribution(jobPostIds);
        List<ScoreDistributionDTO> scoreDist = distribution.stream()
            .map(row -> new ScoreDistributionDTO((String) row[0], ((Number) row[1]).longValue()))
            .collect(Collectors.toList());

        List<Object[]> funnelData = applicationRepository.getFunnelByJobPostIds(jobPostIds);
        List<StatusCountDTO> funnel = funnelData.stream()
            .map(row -> new StatusCountDTO((String) row[0], ((Number) row[1]).longValue()))
            .collect(Collectors.toList());

        return new JobPostOverviewDTO(scoreDist, funnel);
    }
}
