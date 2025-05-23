package com.asusoftware.TermoPro.team.service;

import com.asusoftware.TermoPro.exception.ResourceNotFoundException;
import com.asusoftware.TermoPro.team.model.Team;
import com.asusoftware.TermoPro.team.model.TeamMember;
import com.asusoftware.TermoPro.team.model.TeamMemberId;
import com.asusoftware.TermoPro.team.model.dto.TeamDto;
import com.asusoftware.TermoPro.team.model.dto.UpdateTeamDto;
import com.asusoftware.TermoPro.team.repository.TeamMembersRepository;
import com.asusoftware.TermoPro.team.repository.TeamRepository;
import com.asusoftware.TermoPro.user.model.User;
import com.asusoftware.TermoPro.user.model.UserRole;
import com.asusoftware.TermoPro.user.model.dto.UserDto;
import com.asusoftware.TermoPro.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class TeamService {

    private final TeamRepository teamRepository;
    private final TeamMembersRepository teamMembersRepository;
    private final UserRepository userRepository;
    private final ModelMapper mapper;

    @Transactional
    public TeamDto createTeam(String name, UUID companyId, UUID userId) {
        User requester = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Utilizatorul nu a fost găsit."));
        validateUserCanManageTeams(requester.getId(), companyId);
        Team team = Team.builder()
                .name(name)
                .companyId(companyId)
                .createdAt(LocalDateTime.now())
                .build();
        teamRepository.save(team);
        return mapper.map(team, TeamDto.class);
    }

    @Transactional
    public void addUserToTeam(UUID teamId, UUID userId, UUID requesterId) {
        Team team = teamRepository.findById(teamId)
                .orElseThrow(() -> new ResourceNotFoundException("Echipa nu există."));
        User requester = userRepository.findById(requesterId)
                .orElseThrow(() -> new ResourceNotFoundException("Utilizatorul nu a fost găsit."));
        validateUserCanManageTeams(requester.getId(), team.getCompanyId());

        if (!userRepository.existsById(userId)) {
            throw new ResourceNotFoundException("Utilizatorul nu există.");
        }
        if (teamMembersRepository.existsByTeamIdAndUserId(teamId, userId)) {
            throw new IllegalArgumentException("Utilizatorul este deja în echipă.");
        }

        TeamMember member = TeamMember.builder()
                .teamId(teamId)
                .userId(userId)
                .joinedAt(LocalDateTime.now())
                .build();

        teamMembersRepository.save(member);
    }

    @Transactional
    public void removeUserFromTeam(UUID teamId, UUID userId, UUID requesterId) {
        User requester = userRepository.findById(requesterId)
                .orElseThrow(() -> new ResourceNotFoundException("Utilizatorul nu a fost găsit."));
        Team team = teamRepository.findById(teamId)
                .orElseThrow(() -> new ResourceNotFoundException("Echipa nu există."));
        validateUserCanManageTeams(requester.getId(), team.getCompanyId());
        TeamMemberId id = new TeamMemberId(teamId, userId);
        if (!teamMembersRepository.existsById(id)) {
            throw new ResourceNotFoundException("Utilizatorul nu este în echipă.");
        }
        teamMembersRepository.deleteById(id);
    }

    public List<UserDto> getUsersInTeam(UUID teamId) {
        List<TeamMember> members = teamMembersRepository.findAllByTeamId(teamId);
        return members.stream()
                .map(member -> userRepository.findById(member.getUserId())
                        .orElseThrow(() -> new ResourceNotFoundException("Utilizatorul nu a fost găsit.")))
                .map(user -> mapper.map(user, UserDto.class))
                .collect(Collectors.toList());
    }

    public List<TeamDto> getTeamsInCompany(UUID companyId) {
        List<Team> teams = teamRepository.findAllByCompanyId(companyId);
        return teams.stream()
                .map(team -> mapper.map(team, TeamDto.class))
                .toList();
    }

    public TeamDto getTeamDetails(UUID teamId) {
        Team team = teamRepository.findById(teamId)
                .orElseThrow(() -> new ResourceNotFoundException("Echipa nu există."));
        return mapper.map(team, TeamDto.class);
    }

    public boolean isUserInTeam(UUID teamId, UUID userId) {
        return teamMembersRepository.existsByTeamIdAndUserId(teamId, userId);
    }

    private void validateUserCanManageTeams(UUID userId, UUID companyId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Userul nu a fost găsit."));

        if (!companyId.equals(user.getCompanyId())) {
            throw new SecurityException("Userul nu aparține companiei.");
        }

        if (user.getRole() != UserRole.OWNER && user.getRole() != UserRole.MANAGER) {
            throw new SecurityException("Doar OWNER sau MANAGER pot modifica echipe.");
        }
    }

    @Transactional
    public TeamDto updateTeam(UUID teamId, UpdateTeamDto dto) {
        Team team = teamRepository.findById(teamId)
                .orElseThrow(() -> new ResourceNotFoundException("Echipa nu a fost găsită."));

        User requester = userRepository.findById(dto.getRequesterId())
                .orElseThrow(() -> new ResourceNotFoundException("Utilizatorul nu a fost găsit."));

        validateUserCanManageTeams(requester.getId(), team.getCompanyId());

        team.setName(dto.getName());
        teamRepository.save(team);

        return mapper.map(team, TeamDto.class);
    }

    @Transactional
    public void deleteTeam(UUID teamId, UUID requesterId) {
        Team team = teamRepository.findById(teamId)
                .orElseThrow(() -> new ResourceNotFoundException("Echipa nu a fost găsită."));

        User requester = userRepository.findById(requesterId)
                .orElseThrow(() -> new ResourceNotFoundException("Utilizatorul nu a fost găsit."));

        validateUserCanManageTeams(requester.getId(), team.getCompanyId());

        // Stergem membrii echipei mai întâi (dacă vrei să previi FK constraint error)
        teamMembersRepository.deleteAllByTeamId(teamId);

        teamRepository.delete(team);
    }

}
