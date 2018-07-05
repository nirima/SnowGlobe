package com.nirima.snowglobe.repository;

import com.nirima.snowglobe.web.data.Globe;

import java.util.Collection;

/**
 * API To get/set items from a repository (filesystem)
 */
public interface IRepository {

  /**
   * Get an accessor for this repository
   * @param id
   * @return
   */
  IRepositoryItem forGlobe(String id);

  /**
   * Get a list of globes contained in this repository.
   * @return
   */
  Collection<Globe> list();
}
