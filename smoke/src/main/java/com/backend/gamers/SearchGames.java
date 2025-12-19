package com.backend.gamers;

import com.backend.daos.CategoryDao;
import com.backend.daos.CompanyDao;
import com.backend.daos.VideogameCategoryDao;
import com.backend.daos.VideogameDao;
import com.backend.entities.Category;
import com.backend.entities.Company;
import com.backend.entities.Videogame;
import com.backend.entities.VideogameCategory;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author antho
 */
public class SearchGames {

    public List<Videogame> search(String type, String value) throws SQLException {
        List<Videogame> result = new ArrayList<>();
        VideogameDao dao = new VideogameDao("videogame", "videogame_id");

        switch (type.toLowerCase()) {
            case "name":
                Videogame vg = dao.readByColumn(value, "name");
                if (vg != null) {
                    result.add(vg);
                }
                break;
            case "category":
                CategoryDao catDao = new CategoryDao("category", "category_id");
                Category cat = catDao.readByColumn(value, "name");
                if (cat != null) {
                    VideogameCategoryDao vcDao = new VideogameCategoryDao("videogameCategory", "videogameCategory_id");
                    List<VideogameCategory> vcs = vcDao.readByCategoryId(cat.getCategoryId());
                    for (VideogameCategory vc : vcs) {
                        Videogame game = dao.readByPk(String.valueOf(vc.getGameId()));
                        if (game != null) {
                            result.add(game);
                        }
                    }
                }
                break;
            case "price":
                Videogame vgPrice = dao.readByColumn(value, "price");
                if (vgPrice != null) {
                    result.add(vgPrice);
                }
                break;
            case "company":
                CompanyDao compDao = new CompanyDao("company", "company_id");
                Company comp = compDao.readByColumn(value, "name");
                if (comp != null) {
                    Videogame vgComp = dao.readByColumn(String.valueOf(comp.getCompanyId()), "company_id");
                    if (vgComp != null) {
                        result.add(vgComp);
                    }
                }
                break;
            default:
                break;
        }

        return result;
    }
}
