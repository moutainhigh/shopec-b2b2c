package net.shopec.dao;

import org.apache.ibatis.annotations.Param;

import net.shopec.entity.Store;
import net.shopec.entity.StorePluginStatus;

/**
 * Dao - 店铺插件状态
 * 
 */
public interface StorePluginStatusDao extends BaseDao<StorePluginStatus> {

	/**
	 * 查找店铺插件状态
	 * 
	 * @param store
	 *            店铺
	 * @param pluginId
	 *            插件ID
	 * @return 店铺插件状态，若不存在则返回null
	 */
	StorePluginStatus findByStore(@Param("store")Store store, @Param("pluginId")String pluginId);
}