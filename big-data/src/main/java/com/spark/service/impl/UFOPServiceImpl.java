package com.spark.service.impl;

import com.core.models.FOP;
import com.core.models.UO;
import com.core.models.UOHistory;
import com.google.inject.Inject;
import com.spark.repository.FOPRepository;
import com.spark.repository.UORepository;
import com.spark.service.UFOPService;

import java.util.List;

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
        return uoRepository.findOne(id).getData();
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
