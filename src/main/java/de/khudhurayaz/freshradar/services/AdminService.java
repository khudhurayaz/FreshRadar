package de.khudhurayaz.freshradar.services;

import de.khudhurayaz.freshradar.dto.*;
import de.khudhurayaz.freshradar.model.Profile;
import de.khudhurayaz.freshradar.model.User;
import de.khudhurayaz.freshradar.model.setting.PagesSetting;
import de.khudhurayaz.freshradar.services.setting.PagesSettingService;
import lombok.AllArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Log4j2
@Service
@AllArgsConstructor
public class AdminService {
    private final ProfileServices profileServices;
    private final ProductService productService;
    private final CategoryService categoryService;
    private final LocationService locationService;
    private final SubscriptionService subscriptionService;
    private final InventoryService inventoryService;
    private final ContactService contactService;
    private final UserService userService;
    private final PagesSettingService pagesSettingService;


    public Optional<ProfileRequest> getProfileRequest(String email){
        return profileServices.findByUserEmail(email);
    }

    public User findAdminId(int id) {
        return userService.findById(id);
    }

    public User findAdminByEmail(String email) {
        return userService.findByEmail(email).get();
    }

    public Optional<List<ProfileRequest>> allProfiles(int adminId){
        Optional<List<ProfileRequest>> temp = Optional.empty();
        User user = userService.findById(adminId);
        if (user != null) {
            temp = profileServices.getAll();
        }
        return temp;
    }

    public Optional<List<ProductRequest>> allProducts(){
        return productService.findAll();
    }

    public Optional<Boolean> saveProfile(ProfileRequest request) {
        Optional<Profile> profileOptional =  profileServices.save(request);
        return Optional.of(profileOptional.isPresent());
    }

    @Transactional
    public boolean deleteProfileById(int profileId) {
        log.debug("deleteProfileById start profileId={}", profileId);

        Optional<ProfileRequest> requestOpt = profileServices.findById(profileId);
        log.debug("profileServices.findById done present={}", requestOpt.isPresent());

        if (requestOpt.isEmpty()) {
            return false;
        }

        ProfileRequest request = requestOpt.get();
        log.debug("profile loaded userId={}, email={}", request.getUser().getId(), request.getUser().getEmail());

        boolean deleteInventory = true;
        boolean deleteProduct = true;

        log.debug("before productService.findByUser");
        List<ProductRequest> products = productService.findByUser(request.getUser());
        log.debug("after productService.findByUser count={}", products.size());

        for (ProductRequest product : products) {
            log.debug("deleting productId={}", product.getId());
            boolean inventoryDeleted = inventoryService.deleteByProductId(product.getId());
            boolean productDeleted = productService.delete(product.getId()).isPresent();
            log.debug("productId={} inventoryDeleted={} productDeleted={}", product.getId(), inventoryDeleted, productDeleted);

            deleteInventory = deleteInventory && inventoryDeleted;
            deleteProduct = deleteProduct && productDeleted;
        }

        log.debug("before profile delete");
        boolean deleteProfile = profileServices.delete(request.getId());
        log.debug("after profile delete={}", deleteProfile);

        log.debug("before subscription delete");
        boolean deleteSubscription = subscriptionService.deleteByUser_Id(request.getUser().getId());
        log.debug("after subscription delete={}", deleteSubscription);

        log.debug("before user delete");
        boolean deleteUser = userService.deleteByEmail(request.getUser().getEmail());
        log.debug("after user delete={}", deleteUser);

        boolean result = deleteInventory && deleteProduct && deleteProfile && deleteSubscription && deleteUser;
        log.debug("deleteProfileById result={}", result);

        return result;
    }
    public List<ContactRequest> allContacts() {
        return contactService.findAll();
    }

    public List<User> allUsers(){
        return userService.findAll();
    }

    public List<LocationRequest> allLocations(){
        return locationService.getLocations();
    }

    public List<CategoryRequest> allCategories() {
        return categoryService.findAll();
    }

    public Optional<ProductRequest> findProduct(int productId) {
        return productService.findByProductId(productId);
    }

    public Optional<ProductRequest> getProductRequest(int id) {
        return productService.findByProductId(id);
    }

    @Transactional
    public Optional<Boolean> deleteProduct(ProductRequest request) {
        boolean deleteInventory = true;
        boolean deleteProduct = true;
        Optional<ProductRequest> findProductId = productService.findByProductId(request.getId());

        if (findProductId.isPresent()) {
            int productId = findProductId.get().getId();
            deleteInventory = inventoryService.deleteByProductId(productId);
            deleteProduct = productService.delete(productId).isPresent();
        }

        return Optional.of(deleteInventory && deleteProduct);
    }

    public Optional<Boolean> saveProduct(ProductRequest request) {
        boolean productUpdate =  productService.update(request.getId(), request);
        return Optional.of(productUpdate);
    }

    public Optional<ContactRequest> findMessage(int messageId) {
        return contactService.findById(messageId);
    }

    @Transactional
    public boolean deleteMessage(ContactRequest contactRequest) {
        Optional<Boolean> deleted = contactService.delete(contactRequest);
        return deleted.isPresent();
    }

    public PagesSetting savePagesSetting(int userId, int profileSize, int productSize, int contactSize){
        return pagesSettingService.savePagesSetting(userId, profileSize, productSize, contactSize);
    }

    public PagesSetting savePagesSetting(int userId, int userSize){
        return pagesSettingService.savePagesSetting(userId, userSize);
    }

    public PagesSetting getPagesSetting(int userID){
        return pagesSettingService.getPagesSetting(userID);
    }
}
