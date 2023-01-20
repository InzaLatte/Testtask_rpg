package com.game.service;

import com.game.entity.*;
import com.game.exeptions.*;
import com.game.repository.PlayerRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import javax.transaction.Transactional;

import java.util.Date;
import java.util.List;


@Service
public class PlayerService {
    private final PlayerRepository playerRepository;

    @Autowired
    public PlayerService(PlayerRepository playerRepository) {
        this.playerRepository = playerRepository;
    }


    public Player getPlayerById(Long id) {
        if (id > Long.MAX_VALUE || id <= 0) throw new BadRequestException("Id is invalid");
        return playerRepository.findById(id).orElseThrow(() -> new PlayerNotFoundException("Player not found"));
    }


    public Page<Player> findAll(Specification<Player> specification, Pageable pageable) {
        return playerRepository.findAll(specification, pageable);
    }


    public List<Player> getCount(Specification<Player> specification) {
        return playerRepository.findAll(specification);
    }

    @Transactional
    public void deletePlayer(long id){
        Player player = getPlayerById(id);
        playerRepository.delete(player);
    }

    public Player createPlayer(Player player){
        validPlayer(player);
        updatePlayerLevelAndUntilNextLevel(player);
        return playerRepository.saveAndFlush(player);
    }

    public Player updatePlayer (long id, Player player){
        Player newPlayer = getPlayerById(id);

        if(player.getName() != null) {
            validName(player);
            newPlayer.setName(player.getName());
        }

        if(player.getTitle() != null) {
            validTitle(player);
            newPlayer.setTitle(player.getTitle());
        }

        if(player.getBirthday() != null) {
            validDate(player);
            newPlayer.setBirthday(player.getBirthday());
        }
        if (player.getRace() != null){
            validRace(player);
            newPlayer.setRace(player.getRace());
        }
        if (player.getProfession() != null){
            validProfession(player);
            newPlayer.setProfession(player.getProfession());
        }

        if(!player.getBanned()) {
            newPlayer.setBanned(player.getBanned());
        }

        if(player.getExperience() != null) {
            validExperience(player);
            newPlayer.setExperience(player.getExperience());
        }

        updatePlayerLevelAndUntilNextLevel(newPlayer);

        return playerRepository.save(newPlayer);
    }


    public void validPlayer(Player player){
        validProfession(player);
        validRace(player);
        validExperience(player);
        validDate(player);
        validTitle(player);
        validExperience(player);

    }
    public void validName(Player player){
        String name = player.getName();
        if (name == null || name.length() > 12
                || name.isEmpty())
            throw new BadRequestException("Name is incorrect");
    }

    public void validTitle(Player player){
        String title = player.getTitle();
        if (title == null || title.isEmpty() || title.length() > 30){
            throw new BadRequestException("Title is invalid");
        }
    }

    public void validExperience(Player player){
        int experience = player.getExperience();
         if(experience < 0 || experience > 10_000_000)
             throw new BadRequestException("Experience is invalid");
    }

    public void validDate(Player player){
        Date date = player.getBirthday();

        long date2000 = 946674000482L;
        long date3000 = 32535205199494L;

        if( date == null || date.getTime() < 0 ||
                date.getTime() <= date2000 || date.getTime() >= date3000)
            throw new BadRequestException("Date is invalid");
    }

    public void validRace(Player player){
        if (player.getRace() == null)
            throw new BadRequestException("Race is invalid");
    }

    public void validProfession(Player player){
        if (player.getProfession() == null)
            throw new BadRequestException("Profession is invalid");
    }

    public void updatePlayerLevelAndUntilNextLevel(Player player){
        player.setLevel((int) (Math.sqrt(2500 + 200 * player.getExperience()) - 50) / 100);


        player.setUntilNextLevel(50 * (player.getLevel() + 1) *
                (player.getLevel()+2) - player.getExperience());
    }

    public Specification<Player> sortName(String name){
        return ((root, query, criteriaBuilder) -> name == null?
                null:criteriaBuilder.like(root.get("name"), "%"+name+"%") );
    }

    public Specification<Player> sortTitle(String title){
        return ((root, query, criteriaBuilder) -> title ==null?
                null:criteriaBuilder.like(root.get("title"), "%"+title+"%") );
    }

    public Specification<Player> sortRace(Race race){
        return (root, query, criteriaBuilder) -> race ==null?
                null: criteriaBuilder.equal(root.get("race"), race);
    }

    public Specification<Player> sortProfession(Profession profession){
        return (root, query, criteriaBuilder) -> profession == null?
                null: criteriaBuilder.equal(root.get("profession"), profession);
    }

    public Specification<Player> sortExperience(Integer minExp, Integer maxExp){
        return (root,query,criteriaBuilder)->{
            if (minExp == null && maxExp == null) return null;

            else if (minExp == null) return criteriaBuilder.lessThanOrEqualTo(root.get("experience"), maxExp);

            else if (maxExp == null) return criteriaBuilder.greaterThanOrEqualTo(root.get("experience"), minExp);

            else return criteriaBuilder.between(root.get("experience"), minExp, maxExp);
        };
    }

    public Specification<Player> sortLevel(Integer min, Integer max){
        return (root, query, criteriaBuilder) ->{
            if (min == null && max==null) return null;

            else if (min == null) return criteriaBuilder.lessThanOrEqualTo(root.get("level"), max);

            else if(max == null) return criteriaBuilder.greaterThanOrEqualTo(root.get("level"), min);

            else return criteriaBuilder.between(root.get("level"), min, max);
        };
    }

    public Specification<Player> sortDate(Long min, Long max){
        return (root, query, criteriaBuilder) ->{
            if (min == null && max ==null) return null;

            else if (min == null) return criteriaBuilder.lessThanOrEqualTo(root.get("birthday"), new Date(max));

            else if (max == null) return criteriaBuilder.greaterThanOrEqualTo(root.get("birthday"), new Date(min));

            else return criteriaBuilder.between(root.get("birthday"), new Date(min), new Date(max));
        };
    }

    public Specification<Player> isBanned(Boolean banned){
        return (root, query, criteriaBuilder) ->{

            if(banned == null) return null;

            else if (banned) return criteriaBuilder.isTrue(root.get("banned"));

            else return criteriaBuilder.isFalse(root.get("banned"));
        };
    }





}
