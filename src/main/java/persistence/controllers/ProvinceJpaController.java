package persistence.controllers;

import java.io.Serializable;
import javax.persistence.Query;
import javax.persistence.EntityNotFoundException;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;
import persistence.entities.ShopUser;
import java.util.ArrayList;
import java.util.List;
import javax.annotation.Resource;
import javax.enterprise.context.RequestScoped;
import javax.inject.Named;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.transaction.UserTransaction;
import persistence.controllers.exceptions.IllegalOrphanException;
import persistence.controllers.exceptions.NonexistentEntityException;
import persistence.controllers.exceptions.RollbackFailureException;
import persistence.entities.Province;

/**
 *
 * @author Erika Bourque
 */
@Named
@RequestScoped
public class ProvinceJpaController implements Serializable {
    
    @Resource
    private UserTransaction utx;

    @PersistenceContext
    private EntityManager em;

    public ProvinceJpaController() {
    }

    public void create(Province province) throws RollbackFailureException, Exception {
        if (province.getShopUserList() == null) {
            province.setShopUserList(new ArrayList<ShopUser>());
        }
        try {
            utx.begin();
            List<ShopUser> attachedShopUserList = new ArrayList<ShopUser>();
            for (ShopUser shopUserListShopUserToAttach : province.getShopUserList()) {
                shopUserListShopUserToAttach = em.getReference(shopUserListShopUserToAttach.getClass(), shopUserListShopUserToAttach.getId());
                attachedShopUserList.add(shopUserListShopUserToAttach);
            }
            province.setShopUserList(attachedShopUserList);
            em.persist(province);
            for (ShopUser shopUserListShopUser : province.getShopUserList()) {
                Province oldProvinceIdOfShopUserListShopUser = shopUserListShopUser.getProvinceId();
                shopUserListShopUser.setProvinceId(province);
                shopUserListShopUser = em.merge(shopUserListShopUser);
                if (oldProvinceIdOfShopUserListShopUser != null) {
                    oldProvinceIdOfShopUserListShopUser.getShopUserList().remove(shopUserListShopUser);
                    oldProvinceIdOfShopUserListShopUser = em.merge(oldProvinceIdOfShopUserListShopUser);
                }
            }
            utx.commit();
        } catch (Exception ex) {
            try {
                utx.rollback();
            } catch (Exception re) {
                throw new RollbackFailureException("An error occurred attempting to roll back the transaction.", re);
            }
            throw ex;
        }
    }

    public void edit(Province province) throws IllegalOrphanException, NonexistentEntityException, RollbackFailureException, Exception {
        try {
            utx.begin();
            Province persistentProvince = em.find(Province.class, province.getId());
            List<ShopUser> shopUserListOld = persistentProvince.getShopUserList();
            List<ShopUser> shopUserListNew = province.getShopUserList();
            List<String> illegalOrphanMessages = null;
            for (ShopUser shopUserListOldShopUser : shopUserListOld) {
                if (!shopUserListNew.contains(shopUserListOldShopUser)) {
                    if (illegalOrphanMessages == null) {
                        illegalOrphanMessages = new ArrayList<String>();
                    }
                    illegalOrphanMessages.add("You must retain ShopUser " + shopUserListOldShopUser + " since its provinceId field is not nullable.");
                }
            }
            if (illegalOrphanMessages != null) {
                throw new IllegalOrphanException(illegalOrphanMessages);
            }
            List<ShopUser> attachedShopUserListNew = new ArrayList<ShopUser>();
            for (ShopUser shopUserListNewShopUserToAttach : shopUserListNew) {
                shopUserListNewShopUserToAttach = em.getReference(shopUserListNewShopUserToAttach.getClass(), shopUserListNewShopUserToAttach.getId());
                attachedShopUserListNew.add(shopUserListNewShopUserToAttach);
            }
            shopUserListNew = attachedShopUserListNew;
            province.setShopUserList(shopUserListNew);
            province = em.merge(province);
            for (ShopUser shopUserListNewShopUser : shopUserListNew) {
                if (!shopUserListOld.contains(shopUserListNewShopUser)) {
                    Province oldProvinceIdOfShopUserListNewShopUser = shopUserListNewShopUser.getProvinceId();
                    shopUserListNewShopUser.setProvinceId(province);
                    shopUserListNewShopUser = em.merge(shopUserListNewShopUser);
                    if (oldProvinceIdOfShopUserListNewShopUser != null && !oldProvinceIdOfShopUserListNewShopUser.equals(province)) {
                        oldProvinceIdOfShopUserListNewShopUser.getShopUserList().remove(shopUserListNewShopUser);
                        oldProvinceIdOfShopUserListNewShopUser = em.merge(oldProvinceIdOfShopUserListNewShopUser);
                    }
                }
            }
            utx.commit();
        } catch (Exception ex) {
            try {
                utx.rollback();
            } catch (Exception re) {
                throw new RollbackFailureException("An error occurred attempting to roll back the transaction.", re);
            }
            String msg = ex.getLocalizedMessage();
            if (msg == null || msg.length() == 0) {
                Integer id = province.getId();
                if (findProvince(id) == null) {
                    throw new NonexistentEntityException("The province with id " + id + " no longer exists.");
                }
            }
            throw ex;
        }
    }

    public void destroy(Integer id) throws IllegalOrphanException, NonexistentEntityException, RollbackFailureException, Exception {
        try {
            utx.begin();
            Province province;
            try {
                province = em.getReference(Province.class, id);
                province.getId();
            } catch (EntityNotFoundException enfe) {
                throw new NonexistentEntityException("The province with id " + id + " no longer exists.", enfe);
            }
            List<String> illegalOrphanMessages = null;
            List<ShopUser> shopUserListOrphanCheck = province.getShopUserList();
            for (ShopUser shopUserListOrphanCheckShopUser : shopUserListOrphanCheck) {
                if (illegalOrphanMessages == null) {
                    illegalOrphanMessages = new ArrayList<String>();
                }
                illegalOrphanMessages.add("This Province (" + province + ") cannot be destroyed since the ShopUser " + shopUserListOrphanCheckShopUser + " in its shopUserList field has a non-nullable provinceId field.");
            }
            if (illegalOrphanMessages != null) {
                throw new IllegalOrphanException(illegalOrphanMessages);
            }
            em.remove(province);
            utx.commit();
        } catch (Exception ex) {
            try {
                utx.rollback();
            } catch (Exception re) {
                throw new RollbackFailureException("An error occurred attempting to roll back the transaction.", re);
            }
            throw ex;
        }
    }

    public List<Province> findProvinceEntities() {
        return findProvinceEntities(true, -1, -1);
    }

    public List<Province> findProvinceEntities(int maxResults, int firstResult) {
        return findProvinceEntities(false, maxResults, firstResult);
    }

    private List<Province> findProvinceEntities(boolean all, int maxResults, int firstResult) {
        CriteriaQuery cq = em.getCriteriaBuilder().createQuery();
        cq.select(cq.from(Province.class));
        Query q = em.createQuery(cq);
        if (!all) {
            q.setMaxResults(maxResults);
            q.setFirstResult(firstResult);
        }
        return q.getResultList();
    }

    public Province findProvince(Integer id) {
        return em.find(Province.class, id);
    }

    public int getProvinceCount() {
        CriteriaQuery cq = em.getCriteriaBuilder().createQuery();
        Root<Province> rt = cq.from(Province.class);
        cq.select(em.getCriteriaBuilder().count(rt));
        Query q = em.createQuery(cq);
        return ((Long) q.getSingleResult()).intValue();
    }

}
