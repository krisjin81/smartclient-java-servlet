isc.FilterBuilder.create({
    ID: "advancedFilter",
    dataSource: "supplyItem",
    topOperator: "and"
});

isc.ListGrid.create({
    ID: "supplyItemGrid",
    width: 700, 
    height: 224, 
    alternateRecordStyles: true,
    dataSource: supplyItem,
    autoFetchData:true,
    dataPageSize:20,
    canEdit:true,
    canRemoveRecords:true,
    showFilterEditor: true,
    autoSaveEdits: false
});

isc.IButton.create({
    ID: "filterButton",
    title: "Filter",
    click: function () {
        supplyItemGrid.filterData(advancedFilter.getCriteria());
    }
});

isc.HStack.create({
    membersMargin: 10,
    ID: "gridButtons",
    members: [
        isc.DynamicForm.create({
            values: { dataSource: "Change DataSource" },
            items: [
                { name: "dataSource", showTitle: false, editorType: "select",
                    valueMap: ["supplyItem", "employee"],
                    change: function (form, item, value, oldValue) {
                        if (!this.valueMap.contains(value)) return false;
                        else {
                            supplyItemGrid.setDataSource(value);
                            advancedFilter.setDataSource(value);
                            supplyItemGrid.filterData(advancedFilter.getCriteria());
                        }
                    }
                }
            ]
        }),
        isc.IButton.create({
            top: 250,
            title: "Edit New",
            click: "supplyItemGrid.startEditingNew()"
        }),

        isc.IButton.create({
            top: 250,
            left: 100,
            title: "Save all",
            click: "supplyItemGrid.saveAllEdits()"
        }),
    ]
});

isc.VStack.create({
    membersMargin: 10,
    members: [advancedFilter, filterButton, supplyItemGrid, gridButtons]
});
