package cn.crap.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import cn.crap.framework.JsonResult;
import cn.crap.framework.auth.AuthPassport;
import cn.crap.framework.base.BaseController;
import cn.crap.inter.service.ICacheService;
import cn.crap.inter.service.IMenuService;
import cn.crap.model.Menu;
import cn.crap.utils.Const;
import cn.crap.utils.MyString;
import cn.crap.utils.Tools;

@Scope("prototype")
@Controller
@RequestMapping("/menu")
public class MenuController extends BaseController<Menu> {
	@Autowired
	IMenuService menuService;
	@Autowired
	ICacheService cacheService;

	/**
	 * 根据父菜单、菜单名、菜单类型及页码获取菜单列表
	 * 
	 * @return
	 */
	@RequestMapping("/list.do")
	@ResponseBody
	public JsonResult list(@ModelAttribute Menu menu, @RequestParam(defaultValue = "1") Integer currentPage) {
		page.setCurrentPage(currentPage);
		map = Tools.getMap("parentId", menu.getParentId(), "menuName|like", menu.getMenuName(), "type", menu.getType());
		return new JsonResult(1, menuService.findByMap(map, page, null), page);
	}

	@RequestMapping("/detail.do")
	@ResponseBody
	public JsonResult detail(@ModelAttribute Menu menu) {
		if (!menu.getId().equals(Const.NULL_ID)) {
			model = menuService.get(menu.getId());
		} else {
			model = new Menu();
			model.setParentId(menu.getParentId());
			Menu parentMenu = menuService.get(menu.getParentId());
			model.setType(parentMenu.getType());
		}
		return new JsonResult(1, model);
	}

	/**
	 * 
	 * @param menu
	 * @return
	 */
	@RequestMapping("/addOrUpdate.do")
	@ResponseBody
	@AuthPassport(authority = Const.AUTH_MENU)
	public JsonResult addOrUpdate(@ModelAttribute Menu menu) {
		try {
			// 子菜单类型和父菜单类型一致
			Menu parentMenu = menuService.get(menu.getParentId());
			if (parentMenu != null && parentMenu.getId()!=null)
				menu.setType(parentMenu.getType());

			if (!MyString.isEmpty(menu.getId())) {
				menuService.update(menu);
			} else {
				menu.setId(null);
				menuService.save(menu);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		// 清除缓存
		cacheService.delObj("cache:leftMenu");
		return new JsonResult(1, menu);
	}

	@RequestMapping("/delete.do")
	@ResponseBody
	@AuthPassport(authority = Const.AUTH_MENU)
	public JsonResult delete(@ModelAttribute Menu menu) {
		menuService.delete(menu);
		// 清除缓存
		cacheService.delObj("cache:leftMenu");
		return new JsonResult(1, null);
	}

	/****
	 * 后台加载菜单列表
	 */
	@RequestMapping("/menu.do")
	@ResponseBody
	public JsonResult menu() {
		return new JsonResult(1, menuService.getLeftMenu(map));
	}

	
	
	@RequestMapping("/changeSequence.do")
	@ResponseBody
	@AuthPassport
	@Override
	public JsonResult changeSequence(@RequestParam String id,@RequestParam String changeId) {
		Menu change = menuService.get(changeId);
		model = menuService.get(id);
		int modelSequence = model.getSequence();
		
		model.setSequence(change.getSequence());
		change.setSequence(modelSequence);
		
		menuService.update(model);
		menuService.update(change);
		
		// 清除缓存
		cacheService.delObj("cache:leftMenu");
		return new JsonResult(1, null);
	}

}
