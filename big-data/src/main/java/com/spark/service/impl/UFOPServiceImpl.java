package com.spark.service.impl;

import com.core.models.FOP;
import com.core.models.UO;
import com.core.models.UOHistory;
import com.core.service.UFOPService;
import com.google.inject.Inject;
import com.spark.repository.FOPRepository;
import com.spark.repository.UORepository;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * @author Taras Zubrei
 */
public class UFOPServiceImpl implements UFOPService {
    private final UORepository uoRepository;
    private final FOPRepository fopRepository;

    @Inject
    public UFOPServiceImpl(UORepository uoRepository, FOPRepository fopRepository) {
        this.uoRepository = uoRepository;
        this.fopRepository = fopRepository;
    }

    @Override
    public List<UO> findUO(String id) {
        return Optional.ofNullable(uoRepository.findOne(id)).map(UOHistory::getData).orElseGet(ArrayList::new);
    }

    @Override
    public List<UOHistory> findPagedUO(Integer page) {
        return uoRepository.findPaged(page);
    }

    @Override
    public List<FOP> findPagedFOP(Integer page) {
        return fopRepository.findPaged(page);
    }
}
