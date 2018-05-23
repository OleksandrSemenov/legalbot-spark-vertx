package com.core.service;

import com.core.models.FOP;
import com.core.models.UO;
import com.core.models.UOHistory;

import java.util.List;

/**
 * @author Taras Zubrei
 */
public interface UFOPService {
    List<UO> findUO(String id);

    List<UOHistory> findPagedUO(Integer page);

    List<FOP> findPagedFOP(Integer page);
}
