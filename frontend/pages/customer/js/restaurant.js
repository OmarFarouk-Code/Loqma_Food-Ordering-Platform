document.addEventListener("DOMContentLoaded", () => {
    loadMenuItems(); //requires restaurant id as parameter
})

const restaurantURL = `http://127.0.0.1:8080/api/customer/restaurants`;
const menuURL = `http://127.0.0.1:8080/api/restaurants`


async function loadMenuItems(restaurantId) {
    const menuSections = document.getElementById("menu-sections");
    menuSections.innerHTML = "";

    const response = await fetch(menuURL+`/${restaurantId}/menu`, {
        method: "GET",
        headers: { "Content-Type": "application/json"}
    });
    const menuItems = await response.json();

    // console.log(menuItems);

    menuItems.categories.forEach(category => {
        menuSections.innerHTML += `
        <section class="menu-section" id="category-${category.name}">
            <h2 class="menu-section__title" id="category-${category.name}-title">${category.name}</h2>
            <div class="menu-grid" id="${category.name}">
            </div>
        </section>
        `;
        
        const categoryList = document.getElementById(`${category.name}`);
        let count = 1;
        category.items.forEach(menuItem => {
            if(menuItem.description == null) menuItem.description = ""
            categoryList.innerHTML += `
            <article class="menu-card" id="menu-item-${count}" data-item-id="${menuItem.id}">
            <div class="menu-card__body">
                <div>
                <div class="menu-card__name-row">
                    <h3 class="menu-card__name" id="menu-item-${count}-name">${menuItem.name}</h3>
                </div>
                <p class="menu-card__desc" id="menu-item-${count}-description">${menuItem.description}</p>
                </div>
                <p class="menu-card__price" id="menu-item-${count}-price">${menuItem.price} EGP</p>
            </div>
            <div class="menu-card__image-box">
                <img class="menu-card__image" id="menu-item-${count}-image">
                <button type="button" class="menu-card__add" id="menu-item-${count}-add"><span class="material-symbols-outlined icon-20">add</span></button>
            </div>
            </article>
            `;
            count++;
        })
    });
}


    
        

